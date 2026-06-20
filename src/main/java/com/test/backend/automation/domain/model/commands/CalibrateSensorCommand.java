package com.test.backend.automation.domain.model.commands;

public record CalibrateSensorCommand(
    Long sensorConfigurationId,
    String certificateId,
    java.time.LocalDate expirationDate,
    java.time.LocalDateTime calibratedAt
) {}
