package com.test.backend.labs.interfaces.rest.resources;

import java.util.Date;

public record CreateLaboratoryResource(
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
    Date nextMaintenance,
    Integer maintenanceDaysLeft,
    SensorConfigResource sensors,
    SafetyThresholdsResource thresholds,
    NotificationPreferencesResource notifications
) {}
