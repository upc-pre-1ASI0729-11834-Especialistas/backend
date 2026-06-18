package com.test.backend.automation.interfaces.rest.resources;

public record CalibrateSensorResource(
    String certificateId,
    java.time.LocalDate expirationDate,
    java.time.LocalDateTime calibratedAt
) {}
