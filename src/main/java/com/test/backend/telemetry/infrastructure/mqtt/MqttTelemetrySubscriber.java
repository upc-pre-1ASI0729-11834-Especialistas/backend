package com.test.backend.telemetry.infrastructure.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.backend.automation.domain.model.aggregates.SensorConfiguration;
import com.test.backend.automation.infrastructure.persistence.jpa.repositories.SensorConfigurationRepository;
import com.test.backend.labs.domain.model.aggregates.Laboratory;
import com.test.backend.labs.domain.model.entities.LabMetric;
import com.test.backend.labs.infrastructure.persistence.jpa.repositories.LaboratoryRepository;
import com.test.backend.telemetry.domain.model.aggregates.SensorReading;
import com.test.backend.telemetry.infrastructure.persistence.jpa.repositories.SensorReadingRepository;
import com.test.backend.shared.infrastructure.persistence.configuration.MqttProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;

@Service
public class MqttTelemetrySubscriber implements MqttCallbackExtended {

    private static final Logger logger = LoggerFactory.getLogger(MqttTelemetrySubscriber.class);

    private final MqttClient client;
    private final MqttConnectOptions connectOptions;
    private final MqttProperties mqttProperties;
    private final SensorConfigurationRepository sensorConfigurationRepository;
    private final LaboratoryRepository laboratoryRepository;
    private final SensorReadingRepository sensorReadingRepository;
    private final ObjectMapper objectMapper;

    public MqttTelemetrySubscriber(MqttClient client,
                                   MqttConnectOptions connectOptions,
                                   MqttProperties mqttProperties,
                                   SensorConfigurationRepository sensorConfigurationRepository,
                                   LaboratoryRepository laboratoryRepository,
                                   SensorReadingRepository sensorReadingRepository) {
        this.client = client;
        this.connectOptions = connectOptions;
        this.mqttProperties = mqttProperties;
        this.sensorConfigurationRepository = sensorConfigurationRepository;
        this.laboratoryRepository = laboratoryRepository;
        this.sensorReadingRepository = sensorReadingRepository;
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void init() {
        client.setCallback(this);
        new Thread(this::connect).start();
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (client.isConnected()) {
                client.disconnect();
            }
            client.close();
            logger.info("MQTT Client disconnected successfully.");
        } catch (MqttException e) {
            logger.error("Error closing MQTT Client", e);
        }
    }

    private void connect() {
        int retryDelay = 5000;
        while (!client.isConnected()) {
            try {
                logger.info("Attempting to connect to MQTT broker: {}", client.getServerURI());
                client.connect(connectOptions);
                logger.info("Successfully connected to MQTT broker.");
                break;
            } catch (MqttException e) {
                logger.error("Failed to connect to MQTT broker. Retrying in {} seconds...", retryDelay / 1000, e);
                try {
                    Thread.sleep(retryDelay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        try {
            String topic = mqttProperties.getTopic() != null ? mqttProperties.getTopic() : "safelab/#";
            client.subscribe(topic, 1);
            logger.info("Subscribed to MQTT topic: {}", topic);
        } catch (MqttException e) {
            logger.error("Failed to subscribe to MQTT topic after connection", e);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        logger.warn("MQTT connection lost! Reconnecting automatically...", cause);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
        logger.debug("MQTT message arrived on topic: {}, payload: {}", topic, payload);
        
        try {
            String[] segments = topic.split("/");
            if (segments.length < 3 || !"safelab".equalsIgnoreCase(segments[0])) {
                return;
            }

            String unit = segments[1];
            String type = segments[2];

            Optional<SensorConfiguration> sensorOpt = sensorConfigurationRepository.findByUnit(unit);
            if (sensorOpt.isEmpty()) {
                logger.warn("Security warning: received message for unregistered sensor unit '{}' on topic '{}'", unit, topic);
                return;
            }

            SensorConfiguration sensor = sensorOpt.get();

            if ("status".equalsIgnoreCase(type)) {
                if ("OFFLINE".equalsIgnoreCase(payload)) {
                    sensor.setStatus("OFFLINE");
                    sensorConfigurationRepository.save(sensor);
                    logger.info("Sensor unit '{}' is now OFFLINE (LWT).", unit);
                }
            } else if ("data".equalsIgnoreCase(type)) {
                processTelemetryData(sensor, payload);
            }
        } catch (Exception e) {
            logger.error("Error processing MQTT message on topic: " + topic, e);
        }
    }

    private void processTelemetryData(SensorConfiguration sensor, String payload) throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Object> data = objectMapper.readValue(payload, Map.class);
        
        Long timestamp = null;
        if (data.containsKey("timestamp")) {
            Object tsObj = data.get("timestamp");
            if (tsObj instanceof Number) {
                timestamp = ((Number) tsObj).longValue();
            }
        }
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }

        LocalDateTime recordedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());

        @SuppressWarnings("unchecked")
        Map<String, Object> metrics = (Map<String, Object>) data.get("metrics");
        if (metrics == null || metrics.isEmpty()) {
            return;
        }

        // Resolve Laboratory context: try explicitly linked laboratory first, then fallback to sensor name split
        Laboratory lab = sensor.getLaboratory();
        if (lab == null) {
            String name = sensor.getSensorName();
            String[] parts = name.split(" - ");
            String labName = parts.length > 1 ? parts[parts.length - 1].trim() : "";
            
            Optional<Laboratory> labOpt = laboratoryRepository.findByName(labName);
            lab = labOpt.orElseGet(() -> 
                laboratoryRepository.findAll().stream().findFirst().orElse(null)
            );
        }

        if (lab == null) {
            logger.warn("Could not find any Laboratory to associate telemetry for sensor '{}'", sensor.getSensorName());
            return;
        }

        // Process each metric key-value pair
        for (Map.Entry<String, Object> entry : metrics.entrySet()) {
            String key = entry.getKey();
            Double val = null;
            if (entry.getValue() instanceof Number) {
                val = ((Number) entry.getValue()).doubleValue();
            }
            if (val == null) continue;

            // 1. Save historical reading in EAV model
            SensorReading reading = new SensorReading();
            reading.setSensorConfiguration(sensor);
            reading.setLaboratory(lab);
            reading.setMetricKey(key);
            reading.setMetricValue(val);
            reading.setRecordedAt(recordedAt);
            sensorReadingRepository.save(reading);

            // 2. Update real-time metrics for Dashboard display
            updateRealTimeLabMetric(lab, key, val);
        }

        // Update Sensor Configuration Connection Info
        sensor.setStatus("ACTIVE");
        sensor.setLastConnected(recordedAt);
        sensorConfigurationRepository.save(sensor);
    }

    private void updateRealTimeLabMetric(Laboratory lab, String key, Double val) {
        // Find existing metric or create a new one
        LabMetric metric = lab.getMetrics().stream()
                .filter(m -> m.getName().equalsIgnoreCase(key))
                .findFirst()
                .orElseGet(() -> {
                    LabMetric m = new LabMetric();
                    m.setLaboratory(lab);
                    m.setName(key);
                    m.setUnit(getUnitForMetric(key));
                    m.setIcon(getIconForMetric(key));
                    lab.getMetrics().add(m);
                    return m;
                });

        metric.setValue(String.format("%.2f", val));
        evaluateThresholds(lab, key, val, metric);
        laboratoryRepository.save(lab);
    }

    private void evaluateThresholds(Laboratory lab, String key, Double val, LabMetric metric) {
        var thresholds = lab.getSafetyThresholds();
        if (thresholds == null) return;

        if (key.toLowerCase().contains("temp")) {
            if ((thresholds.getTempMin() != null && val < thresholds.getTempMin()) ||
                (thresholds.getTempMax() != null && val > thresholds.getTempMax())) {
                metric.setStatus("CRITICAL");
                lab.setOverallStatus("CRITICAL");
            } else {
                metric.setStatus("NORMAL");
            }
        } else if (key.toLowerCase().contains("co2") || key.toLowerCase().contains("quality")) {
            if (thresholds.getMaxCo2Ppm() != null && val > thresholds.getMaxCo2Ppm()) {
                metric.setStatus("CRITICAL");
                lab.setOverallStatus("CRITICAL");
            } else {
                metric.setStatus("NORMAL");
            }
        } else if (key.toLowerCase().contains("vibr")) {
            if (thresholds.getMaxVibration() != null && val > thresholds.getMaxVibration()) {
                metric.setStatus("CRITICAL");
                lab.setOverallStatus("CRITICAL");
            } else {
                metric.setStatus("NORMAL");
            }
        }
    }

    private String getUnitForMetric(String key) {
        String k = key.toLowerCase();
        if (k.contains("temp")) return "°C";
        if (k.contains("humid")) return "%";
        if (k.contains("co2")) return "ppm";
        if (k.contains("press")) return "hPa";
        if (k.contains("volt") || k.contains("power")) return "V";
        return "";
    }

    private String getIconForMetric(String key) {
        String k = key.toLowerCase();
        if (k.contains("temp")) return "device_thermostat";
        if (k.contains("humid")) return "water_drop";
        if (k.contains("co2") || k.contains("air")) return "co2";
        if (k.contains("press")) return "compress";
        return "sensors";
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // No-op (we only subscribe)
    }
}
