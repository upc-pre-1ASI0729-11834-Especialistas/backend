package com.test.backend.labs.interfaces.rest.resources;

public record LabAlertResource(
    Long id,
    String title,
    String source,
    String timeAgo,
    String severity
) {}
