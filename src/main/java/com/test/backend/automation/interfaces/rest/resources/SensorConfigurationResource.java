package com.test.backend.automation.interfaces.rest.resources;

public record SensorConfigurationResource(
    Long id,
    String sensorName,
    String type,
    String unit,
    java.time.LocalDate calibrationDate,
    boolean isActive,
    String status,
    java.time.LocalDateTime lastConnected,
    Long laboratoryId,
    String laboratoryName,
    Long equipmentId,
    String equipmentName,
    Double minThreshold,
    Double maxThreshold,
    Double warningThreshold
) {}
