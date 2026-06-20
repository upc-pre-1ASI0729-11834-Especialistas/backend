package com.test.backend.labs.interfaces.rest.resources;

import java.util.List;

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
    List<MetricSubscriptionInputResource> metricSubscriptions,
    NotificationPreferencesResource notifications
) {}
