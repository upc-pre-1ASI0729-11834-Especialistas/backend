package com.test.backend.automation.domain.model.commands;

public record CreateSensorConfigurationCommand(
    String sensorName,
    String type,
    String unit,
    boolean isActive,
    Long laboratoryId
) {}
