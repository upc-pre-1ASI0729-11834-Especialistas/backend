package com.test.backend.automation.interfaces.rest.resources;

public record SecurityAccessResource(
    Long id,
    String permission,
    String role,
    boolean isGranted,
    java.time.LocalDate lastAuditDate
) {}
