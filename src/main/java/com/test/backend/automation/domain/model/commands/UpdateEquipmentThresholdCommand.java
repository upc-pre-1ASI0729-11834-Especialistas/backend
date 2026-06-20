package com.test.backend.automation.domain.model.commands;

public record UpdateEquipmentThresholdCommand(
    Long id,
    String name,
    String icon,
    Long laboratoryId,
    Double minThreshold,
    Double maxThreshold,
    Double warningAt,
    String unit,
    Double currentValue,
    String status
) {}
