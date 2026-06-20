package com.test.backend.telemetry.infrastructure.persistence;

import com.test.backend.telemetry.domain.model.aggregates.MetricType;
import com.test.backend.telemetry.infrastructure.persistence.jpa.repositories.MetricTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds the metric_types catalog table with default metric definitions.
 * Runs once at startup; skips if the table already has data.
 */
@Component
@Order(1)
public class MetricTypeSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(MetricTypeSeeder.class);
    private final MetricTypeRepository metricTypeRepository;

    public MetricTypeSeeder(MetricTypeRepository metricTypeRepository) {
        this.metricTypeRepository = metricTypeRepository;
    }

    @Override
    public void run(String... args) {
        if (metricTypeRepository.count() > 0) {
            logger.info("MetricType catalog already seeded ({} entries). Skipping.", metricTypeRepository.count());
            return;
        }

        logger.info("Seeding MetricType catalog...");

        List<MetricType> defaults = List.of(
            createMetricType("temperature",      "Temperature",       "°C",    "device_thermostat", "ENVIRONMENTAL"),
            createMetricType("humidity",          "Humidity",          "%",     "water_drop",        "ENVIRONMENTAL"),
            createMetricType("co2",              "CO₂ Level",         "ppm",   "co2",               "ENVIRONMENTAL"),
            createMetricType("air_quality",      "Air Quality",       "AQI",   "air",               "ENVIRONMENTAL"),
            createMetricType("vibration",        "Vibration",         "mm/s",  "graphic_eq",        "SAFETY"),
            createMetricType("pressure",         "Pressure",          "hPa",   "compress",          "ENVIRONMENTAL"),
            createMetricType("lighting",         "Lighting",          "lux",   "lightbulb",         "EQUIPMENT"),
            createMetricType("ventilation_flow", "Ventilation Flow",  "m³/h",  "hvac",              "EQUIPMENT"),
            createMetricType("air_conditioning", "Air Conditioning",  "°C",    "ac_unit",           "EQUIPMENT")
        );

        metricTypeRepository.saveAll(defaults);
        logger.info("Seeded {} MetricType entries.", defaults.size());
    }

    private MetricType createMetricType(String key, String displayName, String unit, String icon, String category) {
        MetricType mt = new MetricType();
        mt.setKey(key);
        mt.setDisplayName(displayName);
        mt.setUnit(unit);
        mt.setIcon(icon);
        mt.setCategory(category);
        mt.setActive(true);
        return mt;
    }
}
