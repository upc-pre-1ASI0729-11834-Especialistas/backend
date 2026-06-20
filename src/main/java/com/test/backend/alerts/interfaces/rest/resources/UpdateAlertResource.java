package com.test.backend.alerts.interfaces.rest.resources;

public record UpdateAlertResource(
    String title,
    String description,
    String severity,
    String status,
    String labName,
    String timeAgo,
    Long laboratoryId
) {}
