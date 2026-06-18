package com.test.backend.telemetry.infrastructure.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.backend.automation.domain.model.aggregates.SensorConfiguration;
import com.test.backend.automation.infrastructure.persistence.jpa.repositories.SensorConfigurationRepository;
import com.test.backend.labs.domain.model.aggregates.Laboratory;
import com.test.backend.labs.domain.model.entities.LabMetric;
import com.test.backend.labs.domain.model.entities.LabMetricSubscription;
import com.test.backend.labs.infrastructure.persistence.jpa.repositories.LaboratoryRepository;
import com.test.backend.telemetry.domain.model.aggregates.MetricType;
import com.test.backend.telemetry.domain.model.aggregates.SensorReading;
import com.test.backend.telemetry.infrastructure.persistence.jpa.repositories.MetricTypeRepository;
import com.test.backend.telemetry.infrastructure.persistence.jpa.repositories.SensorReadingRepository;
import com.test.backend.shared.infrastructure.persistence.configuration.MqttProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

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
    private final MetricTypeRepository metricTypeRepository;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;

    public MqttTelemetrySubscriber(MqttClient client,
                                   MqttConnectOptions connectOptions,
                                   MqttProperties mqttProperties,
                                   SensorConfigurationRepository sensorConfigurationRepository,
                                   LaboratoryRepository laboratoryRepository,
                                   SensorReadingRepository sensorReadingRepository,
                                   MetricTypeRepository metricTypeRepository,
                                   PlatformTransactionManager transactionManager) {
        this.client = client;
        this.connectOptions = connectOptions;
        this.mqttProperties = mqttProperties;
        this.sensorConfigurationRepository = sensorConfigurationRepository;
        this.laboratoryRepository = laboratoryRepository;
        this.sensorReadingRepository = sensorReadingRepository;
        this.metricTypeRepository = metricTypeRepository;
        this.objectMapper = new ObjectMapper();
        this.transactionTemplate = new TransactionTemplate(transactionManager);
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

            if ("status".equalsIgnoreCase(type)) {
                if ("OFFLINE".equalsIgnoreCase(payload)) {
                    transactionTemplate.executeWithoutResult(status -> {
                        Optional<SensorConfiguration> sensorOpt = sensorConfigurationRepository.findByUnit(unit);
                        if (sensorOpt.isPresent()) {
                            SensorConfiguration sensor = sensorOpt.get();
                            sensor.setStatus("OFFLINE");
                            sensorConfigurationRepository.save(sensor);
                            logger.info("Sensor unit '{}' is now OFFLINE (LWT).", unit);
                        }
                    });
                }
            } else if ("data".equalsIgnoreCase(type)) {
                transactionTemplate.executeWithoutResult(status -> {
                    try {
                        Optional<SensorConfiguration> sensorOpt = sensorConfigurationRepository.findByUnit(unit);
                        if (sensorOpt.isEmpty()) {
                            logger.warn("Security warning: received message for unregistered sensor unit '{}' on topic '{}'", unit, topic);
                            return;
                        }
                        SensorConfiguration sensor = sensorOpt.get();
                        processTelemetryData(sensor, payload);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
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

        // Resolve Laboratory context
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

        // Process each metric key-value pair using the MetricType catalog
        for (Map.Entry<String, Object> entry : metrics.entrySet()) {
            String key = entry.getKey();
            Double val = null;
            if (entry.getValue() instanceof Number) {
                val = ((Number) entry.getValue()).doubleValue();
            }
            if (val == null) continue;

            // Resolve MetricType from catalog — reject unknown keys
            Optional<MetricType> metricTypeOpt = metricTypeRepository.findByKey(key);
            if (metricTypeOpt.isEmpty()) {
                logger.warn("Unknown metric key '{}' from sensor '{}' — ignoring. Register it in metric_types first.", key, sensor.getSensorName());
                continue;
            }
            MetricType metricType = metricTypeOpt.get();

            // 1. Save historical reading with FK to MetricType
            SensorReading reading = new SensorReading();
            reading.setSensorConfiguration(sensor);
            reading.setLaboratory(lab);
            reading.setMetricType(metricType);
            reading.setMetricValue(val);
            reading.setRecordedAt(recordedAt);
            sensorReadingRepository.save(reading);

            // 2. Update real-time metrics for Dashboard display
            updateRealTimeLabMetric(lab, metricType, val);
        }

        // Update Sensor Configuration Connection Info
        sensor.setStatus("ACTIVE");
        sensor.setLastConnected(recordedAt);
        sensorConfigurationRepository.save(sensor);
    }

    /**
     * Updates or creates a real-time LabMetric using data from the MetricType catalog.
     * No more contains()-based inference — icon, unit come from the catalog.
     */
    private void updateRealTimeLabMetric(Laboratory lab, MetricType metricType, Double val) {
        LabMetric metric = lab.getMetrics().stream()
                .filter(m -> m.getName().equalsIgnoreCase(metricType.getKey()))
                .findFirst()
                .orElseGet(() -> {
                    LabMetric m = new LabMetric();
                    m.setLaboratory(lab);
                    m.setName(metricType.getKey());
                    m.setUnit(metricType.getUnit());
                    m.setIcon(metricType.getIcon());
                    lab.getMetrics().add(m);
                    return m;
                });

        metric.setValue(String.format("%.2f", val));
        evaluateThresholds(lab, metricType, val, metric);
        laboratoryRepository.save(lab);
    }

    /**
     * Evaluates thresholds using the LabMetricSubscription relationship.
     * No more contains()-based string matching — uses exact MetricType reference.
     */
    private void evaluateThresholds(Laboratory lab, MetricType metricType, Double val, LabMetric metric) {
        if (lab.getMetricSubscriptions() == null) return;

        Optional<LabMetricSubscription> subscriptionOpt = lab.getMetricSubscriptions().stream()
                .filter(sub -> sub.getMetricType().getId().equals(metricType.getId()) && sub.isActive())
                .findFirst();

        if (subscriptionOpt.isEmpty()) {
            metric.setStatus("NORMAL");
            return;
        }

        LabMetricSubscription subscription = subscriptionOpt.get();
        boolean breached = false;

        if (subscription.getMinThreshold() != null && val < subscription.getMinThreshold()) {
            breached = true;
        }
        if (subscription.getMaxThreshold() != null && val > subscription.getMaxThreshold()) {
            breached = true;
        }

        if (breached) {
            metric.setStatus("CRITICAL");
            metric.setThreshold(subscription.getMaxThreshold() != null ? subscription.getMaxThreshold() : subscription.getMinThreshold());
            lab.setOverallStatus("CRITICAL");
        } else {
            metric.setStatus("NORMAL");
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // No-op (we only subscribe)
    }
}
