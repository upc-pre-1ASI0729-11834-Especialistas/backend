package com.test.backend.telemetry.interfaces.rest.resources;

public record MetricTypeResource(
    Long id,
    String key,
    String displayName,
    String unit,
    String icon,
    String category,
    boolean active
) {}
