package com.test.backend.history.interfaces.rest.resources;

public record UpdateHistoryRecordResource(
    String name,
    String description,
    java.time.LocalDateTime occurredAt,
    String lab,
    String eventType,
    String severity,
    String status
) {}
