package com.test.backend.telemetry.interfaces.rest.resources;

public record UpdateMetricTypeResource(
    String key,
    String displayName,
    String unit,
    String icon,
    String category,
    Boolean active
) {}
