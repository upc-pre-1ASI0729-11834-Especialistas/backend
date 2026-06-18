package com.test.backend.automation.domain.model.commands;

public record UpdateSensorConfigurationCommand(
    Long id,
    String sensorName,
    String type,
    String unit,
    boolean isActive
) {}
