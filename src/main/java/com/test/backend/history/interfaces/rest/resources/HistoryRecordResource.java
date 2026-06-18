package com.test.backend.history.interfaces.rest.resources;

import java.util.Date;

public record HistoryRecordResource(
    Long id,
    String name,
    String description,
    Date occurredAt,
    String lab,
    String eventType,
    String severity,
    String status
) {}
