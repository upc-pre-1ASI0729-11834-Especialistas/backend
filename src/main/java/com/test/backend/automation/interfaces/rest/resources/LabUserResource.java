package com.test.backend.automation.interfaces.rest.resources;

import java.util.Date;

public record LabUserResource(
    Long id,
    String fullName,
    String email,
    String role,
    String labsAccess,
    Date lastLogin,
    String status,
    String avatarInitials,
    String avatarColor
) {}
