package com.test.backend.labs.domain.model.commands;

import com.test.backend.labs.domain.model.valueobjets.NotificationPreferences;
import com.test.backend.labs.domain.model.valueobjets.SafetyThresholds;
import com.test.backend.labs.domain.model.valueobjets.SensorConfig;
import java.util.Date;

public record CreateLaboratoryCommand(
    String name,
    String type,
    String status,
    String building,
    String floor,
    String labCode,
    String roomNumber,
    String description,
    String overallStatus,
    boolean active,
    boolean isLive,
    Date nextMaintenance,
    Integer maintenanceDaysLeft,
    SensorConfig sensorConfig,
    SafetyThresholds safetyThresholds,
    NotificationPreferences notificationPreferences
) {}
