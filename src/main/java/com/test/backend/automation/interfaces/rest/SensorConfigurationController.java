package com.test.backend.automation.interfaces.rest;

import com.test.backend.automation.domain.model.commands.CreateSensorConfigurationCommand;
import com.test.backend.automation.domain.model.commands.UpdateSensorConfigurationCommand;
import com.test.backend.automation.domain.model.commands.CalibrateSensorCommand;
import com.test.backend.automation.domain.model.aggregates.SensorConfiguration;
import com.test.backend.automation.infrastructure.persistence.jpa.repositories.SensorConfigurationRepository;
import com.test.backend.automation.interfaces.rest.resources.SensorConfigurationResource;
import com.test.backend.automation.interfaces.rest.resources.CalibrateSensorResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
                .map(this::toResource)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PostMapping
    @Operation(summary = "Create a new sensor configuration")
    public ResponseEntity<SensorConfigurationResource> createConfiguration(@RequestBody SensorConfigurationResource resource) {
        var command = new CreateSensorConfigurationCommand(
                resource.sensorName(),
                resource.type(),
                resource.unit(),
                resource.isActive()
        );
        var config = new SensorConfiguration(command);
        sensorConfigurationRepository.save(config);
        return new ResponseEntity<>(toResource(config), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing sensor configuration")
    public ResponseEntity<SensorConfigurationResource> updateConfiguration(@PathVariable Long id, @RequestBody SensorConfigurationResource resource) {
        var result = sensorConfigurationRepository.findById(id);
        if (result.isEmpty()) return ResponseEntity.notFound().build();

        var config = result.get();
        var command = new UpdateSensorConfigurationCommand(
                id,
                resource.sensorName() != null ? resource.sensorName() : config.getSensorName(),
                resource.type() != null ? resource.type() : config.getType(),
                resource.unit() != null ? resource.unit() : config.getUnit(),
                resource.isActive()
        );
        config.updateFrom(command);
        sensorConfigurationRepository.save(config);
        return ResponseEntity.ok(toResource(config));
    }

    @PostMapping("/{id}/calibrate")
    @Operation(summary = "Calibrate a sensor")
    public ResponseEntity<SensorConfigurationResource> calibrateSensor(@PathVariable Long id, @RequestBody CalibrateSensorResource resource) {
        var result = sensorConfigurationRepository.findById(id);
        if (result.isEmpty()) return ResponseEntity.notFound().build();

        var config = result.get();
        var command = new CalibrateSensorCommand(
                id,
                resource.certificateId(),
                resource.expirationDate(),
                resource.calibratedAt()
        );
        config.calibrate(command);
        sensorConfigurationRepository.save(config);
        return ResponseEntity.ok(toResource(config));
    }

    private SensorConfigurationResource toResource(SensorConfiguration config) {
        return new SensorConfigurationResource(
                config.getId(),
                config.getSensorName(),
                config.getType(),
                config.getUnit(),
                config.getCalibrationDate(),
                config.isActive()
        );
    }
}
