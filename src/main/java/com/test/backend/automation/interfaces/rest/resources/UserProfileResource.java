package com.test.backend.automation.interfaces.rest.resources;

public record UserProfileResource(
    Long id,
    String fullName,
    String role,
    String email,
    String avatarUrl,
    String phoneNumber,
    String professionalTitle,
    String employeeId,
    String systemState,
    String accessTier,
    String defaultStartShift,
    String shiftDuration,
    boolean autoGenerateShiftReport
) {}
