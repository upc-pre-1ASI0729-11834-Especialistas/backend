package com.test.backend.automation.interfaces.rest;

import com.test.backend.automation.infrastructure.persistence.jpa.repositories.SensorConfigurationRepository;
import com.test.backend.automation.interfaces.rest.resources.SensorConfigurationResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Sensor Configurations", description = "Sensor Configurations management API")
@RestController
@RequestMapping("/api/v1/sensor-configurations")
public class SensorConfigurationController {

    private final SensorConfigurationRepository sensorConfigurationRepository;

    public SensorConfigurationController(SensorConfigurationRepository sensorConfigurationRepository) {
        this.sensorConfigurationRepository = sensorConfigurationRepository;
    }

    @GetMapping
    @Operation(summary = "Get all sensor configurations")
    public ResponseEntity<List<SensorConfigurationResource>> getAllConfigurations() {
        var configs = sensorConfigurationRepository.findAll();
        var resources = configs.stream()
                .map(c -> new SensorConfigurationResource(
                        c.getId(),
                        c.getSensorName(),
                        c.getType(),
                        c.getUnit(),
                        c.getCalibrationDate(),
                        c.isActive()
                ))
                .toList();
        return ResponseEntity.ok(resources);
    }
}
