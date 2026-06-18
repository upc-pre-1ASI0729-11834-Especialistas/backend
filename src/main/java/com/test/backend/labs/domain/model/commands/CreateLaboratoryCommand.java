package com.test.backend.labs.domain.model.commands;

import com.test.backend.labs.domain.model.valueobjets.NotificationPreferences;

import java.util.List;

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
    java.time.LocalDate nextMaintenance,
    Integer maintenanceDaysLeft,
    List<MetricSubscriptionData> metricSubscriptions,
    NotificationPreferences notificationPreferences
) {
    public record MetricSubscriptionData(
        Long metricTypeId,
        Double minThreshold,
        Double maxThreshold
    ) {}
}
