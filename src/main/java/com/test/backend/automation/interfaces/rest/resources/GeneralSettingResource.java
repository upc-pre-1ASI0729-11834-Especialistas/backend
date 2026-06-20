package com.test.backend.automation.interfaces.rest.resources;

public record GeneralSettingResource(
    Long id,
    String settingKey,
    String value,
    String category,
    String description
) {}
