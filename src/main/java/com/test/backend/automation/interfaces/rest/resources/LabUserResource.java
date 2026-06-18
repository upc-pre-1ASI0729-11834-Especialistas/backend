package com.test.backend.automation.interfaces.rest.resources;

public record LabUserResource(
    Long id,
    String fullName,
    String email,
    String role,
    String labsAccess,
    java.time.LocalDateTime lastLogin,
    String status,
    String avatarInitials,
    String avatarColor
) {}
