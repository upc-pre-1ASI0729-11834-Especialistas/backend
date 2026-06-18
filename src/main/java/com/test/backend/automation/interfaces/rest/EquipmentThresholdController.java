package com.test.backend.automation.interfaces.rest;

import com.test.backend.automation.domain.model.aggregates.EquipmentThreshold;
import com.test.backend.automation.domain.model.commands.UpdateEquipmentThresholdCommand;
import com.test.backend.automation.infrastructure.persistence.jpa.repositories.EquipmentThresholdRepository;
import com.test.backend.automation.interfaces.rest.resources.EquipmentThresholdResource;
import com.test.backend.labs.domain.model.aggregates.Laboratory;
import com.test.backend.labs.infrastructure.persistence.jpa.repositories.LaboratoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Equipment Thresholds", description = "Equipment Thresholds management API")
@RestController
@RequestMapping("/api/v1/equipment-thresholds")
public class EquipmentThresholdController {

    private final EquipmentThresholdRepository equipmentThresholdRepository;
    private final LaboratoryRepository laboratoryRepository;

    public EquipmentThresholdController(EquipmentThresholdRepository equipmentThresholdRepository,
                                        LaboratoryRepository laboratoryRepository) {
        this.equipmentThresholdRepository = equipmentThresholdRepository;
        this.laboratoryRepository = laboratoryRepository;
    }

    @GetMapping
    @Operation(summary = "Get all equipment thresholds")
    public ResponseEntity<List<EquipmentThresholdResource>> getAllThresholds() {
        var thresholds = equipmentThresholdRepository.findAll();
        var resources = thresholds.stream()
                .map(this::toResource)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing equipment threshold")
    public ResponseEntity<EquipmentThresholdResource> updateThreshold(@PathVariable Long id, 
                                                                     @RequestBody EquipmentThresholdResource resource) {
        var result = equipmentThresholdRepository.findById(id);
        if (result.isEmpty()) return ResponseEntity.notFound().build();

        var threshold = result.get();
        
        Laboratory laboratory = threshold.getLaboratory();
        if (resource.lab() != null && !resource.lab().isBlank()) {
            laboratory = laboratoryRepository.findAll().stream()
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
