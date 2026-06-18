package com.test.backend.labs.interfaces.rest.resources;

public record NotificationPreferencesResource(
    boolean email,
    boolean sms,
    boolean push,
    boolean criticalOnly
) {}
