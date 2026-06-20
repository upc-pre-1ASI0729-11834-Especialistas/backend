package com.test.backend.labs.interfaces.rest.resources;

public record LabScheduleResource(
    Long id,
    String name,
    String timeRange,
    boolean active,
    String icon
) {}
