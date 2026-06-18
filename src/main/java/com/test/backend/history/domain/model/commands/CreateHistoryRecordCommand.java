package com.test.backend.history.domain.model.commands;

import java.util.Date;

public record CreateHistoryRecordCommand(
    String name,
    String description,
    Date occurredAt,
    String lab,
    String eventType,
    String severity,
    String status
) {}
