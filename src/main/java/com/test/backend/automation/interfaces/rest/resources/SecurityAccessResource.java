package com.test.backend.automation.interfaces.rest.resources;

import java.util.Date;

public record SecurityAccessResource(
    Long id,
    String permission,
    String role,
    boolean isGranted,
    Date lastAuditDate
) {}
