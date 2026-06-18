package com.test.backend.telemetry.interfaces.rest.resources;

public record TemperatureReadingResource(
    Long id,
    String date,
    Double lab01Value,
    Double lab02Value
) {}
