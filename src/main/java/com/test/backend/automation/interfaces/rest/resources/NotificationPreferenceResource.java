package com.test.backend.automation.interfaces.rest.resources;

public record NotificationPreferenceResource(
    Long id,
    String channel,
    boolean isEnabled,
    String threshold,
    String description
) {}
