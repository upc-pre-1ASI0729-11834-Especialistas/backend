package com.test.backend.history.interfaces.rest.resources;

import java.util.Date;

public record UpdateHistoryRecordResource(
    String name,
    String description,
    Date occurredAt,
    String lab,
    String eventType,
    String severity,
    String status
) {}
