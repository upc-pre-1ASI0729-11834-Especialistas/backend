package com.test.backend.automation.interfaces.rest;

import com.test.backend.automation.domain.model.commands.CreateSensorConfigurationCommand;
import com.test.backend.automation.domain.model.commands.UpdateSensorConfigurationCommand;
import com.test.backend.automation.domain.model.commands.CalibrateSensorCommand;
import com.test.backend.automation.domain.model.aggregates.SensorConfiguration;
import com.test.backend.automation.infrastructure.persistence.jpa.repositories.SensorConfigurationRepository;
import com.test.backend.automation.interfaces.rest.resources.SensorConfigurationResource;
import com.test.backend.automation.interfaces.rest.resources.CalibrateSensorResource;
import com.test.backend.automation.domain.model.aggregates.EquipmentThreshold;
import com.test.backend.automation.infrastructure.persistence.jpa.repositories.EquipmentThresholdRepository;
import com.test.backend.labs.domain.model.aggregates.Laboratory;
import com.test.backend.labs.infrastructure.persistence.jpa.repositories.LaboratoryRepository;
import com.test.backend.shared.application.CurrentWorkspaceService;
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
    private final LaboratoryRepository laboratoryRepository;
    private final EquipmentThresholdRepository equipmentThresholdRepository;
    private final CurrentWorkspaceService currentWorkspaceService;

    public SensorConfigurationController(SensorConfigurationRepository sensorConfigurationRepository,
                                         LaboratoryRepository laboratoryRepository,
                                         EquipmentThresholdRepository equipmentThresholdRepository,
                                         CurrentWorkspaceService currentWorkspaceService) {
        this.sensorConfigurationRepository = sensorConfigurationRepository;
        this.laboratoryRepository = laboratoryRepository;
        this.equipmentThresholdRepository = equipmentThresholdRepository;
        this.currentWorkspaceService = currentWorkspaceService;
    }

    @GetMapping
    @Operation(summary = "Get all sensor configurations")
    public ResponseEntity<List<SensorConfigurationResource>> getAllConfigurations() {
        var profileOpt = currentWorkspaceService.getCurrentUserProfile();
        if (profileOpt.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        var profile = profileOpt.get();
        var configs = sensorConfigurationRepository.findByLaboratoryWorkspaceId(profile.getWorkspaceId());

        if (profile.getRole() != null && "Administrator".equalsIgnoreCase(profile.getRole().getName())) {
            var resources = configs.stream()
                    .map(this::toResource)
                    .toList();
            return ResponseEntity.ok(resources);
        }

        var allowedLabIds = profile.getLabAccesses().stream()
                .map(access -> access.getLaboratory().getId())
                .toList();

        var resources = configs.stream()
                .filter(c -> c.getLaboratory() != null && allowedLabIds.contains(c.getLaboratory().getId()))
                .map(this::toResource)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PostMapping
    @Operation(summary = "Create a new sensor configuration")
    public ResponseEntity<SensorConfigurationResource> createConfiguration(@RequestBody SensorConfigurationResource resource) {
        var workspaceIdOpt = currentWorkspaceService.getCurrentWorkspaceId();
        if (workspaceIdOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Laboratory laboratory = null;
        if (resource.laboratoryId() != null) {
            laboratory = laboratoryRepository.findByIdAndWorkspaceId(resource.laboratoryId(), workspaceIdOpt.get()).orElse(null);
            if (laboratory == null) {
                return ResponseEntity.badRequest().build(); // Laboratory does not exist in user's workspace
            }
        }

        EquipmentThreshold equipment = null;
        if (resource.equipmentId() != null) {
            equipment = equipmentThresholdRepository.findById(resource.equipmentId()).orElse(null);
        }

        var command = new CreateSensorConfigurationCommand(
                resource.sensorName(),
                resource.type(),
                resource.unit(),
                resource.isActive(),
                resource.laboratoryId(),
                resource.equipmentId(),
                resource.minThreshold(),
                resource.maxThreshold(),
                resource.warningThreshold()
        );
        var config = new SensorConfiguration(command, laboratory, equipment);
        sensorConfigurationRepository.save(config);
        return new ResponseEntity<>(toResource(config), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing sensor configuration")
    public ResponseEntity<SensorConfigurationResource> updateConfiguration(@PathVariable Long id, @RequestBody SensorConfigurationResource resource) {
        var workspaceIdOpt = currentWorkspaceService.getCurrentWorkspaceId();
        if (workspaceIdOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var result = sensorConfigurationRepository.findByIdAndLaboratoryWorkspaceId(id, workspaceIdOpt.get());
        if (result.isEmpty()) return ResponseEntity.notFound().build();

        var config = result.get();
        Laboratory laboratory = config.getLaboratory();
        if (resource.laboratoryId() != null) {
            laboratory = laboratoryRepository.findByIdAndWorkspaceId(resource.laboratoryId(), workspaceIdOpt.get()).orElse(null);
            if (laboratory == null) {
                return ResponseEntity.badRequest().build();
            }
        }

        EquipmentThreshold equipment = null;
        if (resource.equipmentId() != null) {
            equipment = equipmentThresholdRepository.findById(resource.equipmentId()).orElse(null);
        }

        var command = new UpdateSensorConfigurationCommand(
                id,
                resource.sensorName() != null ? resource.sensorName() : config.getSensorName(),
                resource.type() != null ? resource.type() : config.getType(),
                resource.unit() != null ? resource.unit() : config.getUnit(),
                resource.isActive(),
                resource.laboratoryId() != null ? resource.laboratoryId() : (config.getLaboratory() != null ? config.getLaboratory().getId() : null),
                resource.equipmentId(),
                resource.minThreshold(),
                resource.maxThreshold(),
                resource.warningThreshold()
        );
        config.updateFrom(command, laboratory, equipment);
        sensorConfigurationRepository.save(config);
        return ResponseEntity.ok(toResource(config));
    }

    @PostMapping("/{id}/calibrate")
    @Operation(summary = "Calibrate a sensor")
    public ResponseEntity<SensorConfigurationResource> calibrateSensor(@PathVariable Long id, @RequestBody CalibrateSensorResource resource) {
        var workspaceIdOpt = currentWorkspaceService.getCurrentWorkspaceId();
        if (workspaceIdOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var result = sensorConfigurationRepository.findByIdAndLaboratoryWorkspaceId(id, workspaceIdOpt.get());
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
                config.isActive(),
                config.getStatus(),
                config.getLastConnected(),
                config.getLaboratory() != null ? config.getLaboratory().getId() : null,
                config.getLaboratory() != null ? config.getLaboratory().getName() : null,
                config.getEquipment() != null ? config.getEquipment().getId() : null,
                config.getEquipment() != null ? config.getEquipment().getName() : null,
                config.getMinThreshold(),
                config.getMaxThreshold(),
                config.getWarningThreshold()
        );
    }
}
