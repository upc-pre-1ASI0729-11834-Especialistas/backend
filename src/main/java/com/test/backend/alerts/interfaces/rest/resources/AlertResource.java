package com.test.backend.alerts.interfaces.rest.resources;

public record AlertResource(
    Long id,
    String title,
    String description,
    String severity,
    String status
) {}
