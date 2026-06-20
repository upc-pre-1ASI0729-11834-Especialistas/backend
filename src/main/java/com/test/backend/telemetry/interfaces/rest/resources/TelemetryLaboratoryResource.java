package com.test.backend.telemetry.interfaces.rest.resources;

public record TelemetryLaboratoryResource(
    Long id,
    String name,
    String type,
    Double temperature,
    String status
) {}
