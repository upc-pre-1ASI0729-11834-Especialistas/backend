package com.test.backend.telemetry.interfaces.rest.resources;

public record CreateMetricTypeResource(
    String key,
    String displayName,
    String unit,
    String icon,
    String category
) {}
