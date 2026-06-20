package com.test.backend.telemetry.domain.model.commands;

public record UpdateMetricTypeCommand(
    Long id,
    String key,
    String displayName,
    String unit,
    String icon,
    String category,
    boolean active
) {}
