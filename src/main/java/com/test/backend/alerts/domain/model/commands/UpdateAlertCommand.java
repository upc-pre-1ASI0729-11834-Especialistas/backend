package com.test.backend.alerts.domain.model.commands;

public record UpdateAlertCommand(
    Long id,
    String title,
    String description,
    String severity,
    String status,
    String labName,
    String timeAgo,
    Long laboratoryId
) {}
