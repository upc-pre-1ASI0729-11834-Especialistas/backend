package com.test.backend.automation.interfaces.rest.resources;

public record EquipmentThresholdResource(
    Long id,
    String name,
    String icon,
    String lab,
    Double minThreshold,
    Double maxThreshold,
    Double warningAt,
    String unit,
    Double currentValue,
    String status
) {}
