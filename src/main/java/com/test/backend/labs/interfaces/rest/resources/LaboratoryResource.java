package com.test.backend.labs.interfaces.rest.resources;

import java.util.Date;
import java.util.List;

public record LaboratoryResource(
    Long id,
    String name,
    String type,
    String status,
    String building,
    String floor,
    String labCode,
    String overallStatus,
    boolean active,
    Date lastUpdate,
    boolean isLive,
    Date nextMaintenance,
    Integer maintenanceDaysLeft,
    String roomNumber,
    String description,
    List<LabMetricResource> metrics,
    List<LabAlertResource> recentAlerts,
    List<LabActivityResource> recentActivities,
    List<LabScheduleResource> schedules,
    SensorConfigResource sensors,
    SafetyThresholdsResource thresholds,
    NotificationPreferencesResource notifications
) {}
