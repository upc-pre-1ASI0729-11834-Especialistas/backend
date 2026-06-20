package com.test.backend.telemetry.interfaces.rest.resources;

public record DashboardStatsResource(
    Long id,
    int totalLaboratories,
    int activeAlerts,
    int systemsHealth,
    int upcomingMaintenance
) {}
