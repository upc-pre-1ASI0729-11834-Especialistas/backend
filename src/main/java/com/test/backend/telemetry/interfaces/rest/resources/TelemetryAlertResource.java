package com.test.backend.telemetry.interfaces.rest.resources;

public record TelemetryAlertResource(
    Long id,
    String labName,
    String title,
    String description,
    String severity,
    String timeAgo
) {}
