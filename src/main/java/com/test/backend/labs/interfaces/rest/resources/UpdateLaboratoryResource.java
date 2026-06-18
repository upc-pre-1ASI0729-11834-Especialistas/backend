package com.test.backend.labs.interfaces.rest.resources;

public record UpdateLaboratoryResource(
    String name,
    String type,
    String status,
    String building,
    String floor,
    String labCode,
    String roomNumber,
    String description,
    String overallStatus,
    Boolean active,
    Boolean isLive,
    java.time.LocalDate nextMaintenance,
    Integer maintenanceDaysLeft,
    SensorConfigResource sensors,
    SafetyThresholdsResource thresholds,
    NotificationPreferencesResource notifications
) {}
