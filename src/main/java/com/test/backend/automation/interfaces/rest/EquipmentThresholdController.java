package com.test.backend.automation.interfaces.rest;

import com.test.backend.automation.domain.model.aggregates.EquipmentThreshold;
import com.test.backend.automation.domain.model.commands.UpdateEquipmentThresholdCommand;
import com.test.backend.automation.infrastructure.persistence.jpa.repositories.EquipmentThresholdRepository;
import com.test.backend.automation.interfaces.rest.resources.EquipmentThresholdResource;
import com.test.backend.labs.domain.model.aggregates.Laboratory;
import com.test.backend.labs.infrastructure.persistence.jpa.repositories.LaboratoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.test.backend.shared.application.CurrentWorkspaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Equipment Thresholds", description = "Equipment Thresholds management API")
@RestController
@RequestMapping("/api/v1/equipment-thresholds")
public class EquipmentThresholdController {

    private final EquipmentThresholdRepository equipmentThresholdRepository;
    private final LaboratoryRepository laboratoryRepository;
    private final CurrentWorkspaceService currentWorkspaceService;

    public EquipmentThresholdController(EquipmentThresholdRepository equipmentThresholdRepository,
                                         LaboratoryRepository laboratoryRepository,
                                         CurrentWorkspaceService currentWorkspaceService) {
        this.equipmentThresholdRepository = equipmentThresholdRepository;
        this.laboratoryRepository = laboratoryRepository;
        this.currentWorkspaceService = currentWorkspaceService;
    }

    @GetMapping
    @Operation(summary = "Get all equipment thresholds")
    public ResponseEntity<List<EquipmentThresholdResource>> getAllThresholds() {
        var profileOpt = currentWorkspaceService.getCurrentUserProfile();
        if (profileOpt.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        var profile = profileOpt.get();
        var thresholds = equipmentThresholdRepository.findByLaboratoryWorkspaceId(profile.getWorkspaceId());

        if (profile.getRole() != null && "Administrator".equalsIgnoreCase(profile.getRole().getName())) {
            var resources = thresholds.stream()
                    .map(this::toResource)
                    .toList();
            return ResponseEntity.ok(resources);
        }

        var allowedLabIds = profile.getLabAccesses().stream()
                .map(access -> access.getLaboratory().getId())
                .toList();

        var resources = thresholds.stream()
                .filter(t -> t.getLaboratory() != null && allowedLabIds.contains(t.getLaboratory().getId()))
                .map(this::toResource)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing equipment threshold")
    public ResponseEntity<EquipmentThresholdResource> updateThreshold(@PathVariable Long id, 
                                                                     @RequestBody EquipmentThresholdResource resource) {
        var workspaceIdOpt = currentWorkspaceService.getCurrentWorkspaceId();
        if (workspaceIdOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Long workspaceId = workspaceIdOpt.get();

        var result = equipmentThresholdRepository.findByIdAndLaboratoryWorkspaceId(id, workspaceId);
        if (result.isEmpty()) return ResponseEntity.notFound().build();

        var threshold = result.get();
        
        Laboratory laboratory = threshold.getLaboratory();
        if (resource.lab() != null && !resource.lab().isBlank()) {
            laboratory = laboratoryRepository.findByWorkspaceId(workspaceId).stream()
                    .filter(l -> l.getName().equalsIgnoreCase(resource.lab()))
                    .findFirst()
                    .orElse(laboratory);
        }

        var command = new UpdateEquipmentThresholdCommand(
                id,
                resource.name() != null ? resource.name() : threshold.getName(),
                resource.icon() != null ? resource.icon() : threshold.getIcon(),
                laboratory != null ? laboratory.getId() : null,
                resource.minThreshold() != null ? resource.minThreshold() : threshold.getMinThreshold(),
                resource.maxThreshold() != null ? resource.maxThreshold() : threshold.getMaxThreshold(),
                resource.warningAt() != null ? resource.warningAt() : threshold.getWarningAt(),
                resource.unit() != null ? resource.unit() : threshold.getUnit(),
                resource.currentValue() != null ? resource.currentValue() : threshold.getCurrentValue(),
                resource.status() != null ? resource.status() : threshold.getStatus()
        );

        threshold.updateFrom(command, laboratory);
        equipmentThresholdRepository.save(threshold);

        return ResponseEntity.ok(toResource(threshold));
    }

    @PostMapping
    @Operation(summary = "Create a new equipment threshold")
    public ResponseEntity<EquipmentThresholdResource> createThreshold(@RequestBody EquipmentThresholdResource resource) {
        var workspaceIdOpt = currentWorkspaceService.getCurrentWorkspaceId();
        if (workspaceIdOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Long workspaceId = workspaceIdOpt.get();

        var laboratoryOpt = laboratoryRepository.findByWorkspaceId(workspaceId).stream()
                .filter(l -> l.getName().equalsIgnoreCase(resource.lab()))
                .findFirst();

        var laboratory = laboratoryOpt.orElseGet(() -> {
            var all = laboratoryRepository.findByWorkspaceId(workspaceId);
            return all.isEmpty() ? null : all.get(0);
        });

        if (laboratory == null) {
            return ResponseEntity.badRequest().build();
        }

        var threshold = new EquipmentThreshold();
        threshold.setLaboratory(laboratory);
        threshold.setName(resource.name() != null ? resource.name() : "New Equipment");
        threshold.setIcon(resource.icon() != null ? resource.icon() : "kitchen");
        threshold.setMinThreshold(resource.minThreshold() != null ? resource.minThreshold() : 0.0);
        threshold.setMaxThreshold(resource.maxThreshold() != null ? resource.maxThreshold() : 100.0);
        threshold.setWarningAt(resource.warningAt() != null ? resource.warningAt() : 80.0);
        threshold.setUnit(resource.unit() != null ? resource.unit() : "°C");
        threshold.setCurrentValue(resource.currentValue() != null ? resource.currentValue() : 20.0);
        threshold.setStatus(resource.status() != null ? resource.status() : "normal");

        equipmentThresholdRepository.save(threshold);
        return new ResponseEntity<>(toResource(threshold), HttpStatus.CREATED);
    }

    private EquipmentThresholdResource toResource(EquipmentThreshold threshold) {
        String labName = threshold.getLaboratory() != null ? threshold.getLaboratory().getName() : "";
        return new EquipmentThresholdResource(
                threshold.getId(),
                threshold.getName(),
                threshold.getIcon(),
                labName,
                threshold.getMinThreshold(),
                threshold.getMaxThreshold(),
                threshold.getWarningAt(),
                threshold.getUnit(),
                threshold.getCurrentValue(),
                threshold.getStatus()
        );
    }
}
