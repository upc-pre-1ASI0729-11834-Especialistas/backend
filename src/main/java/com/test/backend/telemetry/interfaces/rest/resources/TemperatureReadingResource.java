package com.test.backend.telemetry.interfaces.rest.resources;

import java.util.Map;

public record TemperatureReadingResource(
    Long id,
    String date,
    Map<String, Double> values
) {}
