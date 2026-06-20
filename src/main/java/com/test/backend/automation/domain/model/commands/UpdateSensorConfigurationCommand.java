package com.test.backend.automation.domain.model.commands;

public record UpdateSensorConfigurationCommand(
    Long id,
    String sensorName,
    String type,
    String unit,
    boolean isActive,
    Long laboratoryId,
    Long equipmentId,
    Double minThreshold,
    Double maxThreshold,
    Double warningThreshold
) {}
