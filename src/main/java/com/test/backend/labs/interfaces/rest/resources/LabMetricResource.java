package com.test.backend.labs.interfaces.rest.resources;

import java.util.List;

public record LabMetricResource(
    String name,
    String value,
    String unit,
    String status,
    String icon,
    List<Double> sparkline,
    Double threshold,
    String objectType
) {}
