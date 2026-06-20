package com.test.backend.telemetry.domain.model.commands;

public record CreateMetricTypeCommand(
    String key,
    String displayName,
    String unit,
    String icon,
    String category
) {}
