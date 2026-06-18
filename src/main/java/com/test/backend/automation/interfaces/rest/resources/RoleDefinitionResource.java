package com.test.backend.automation.interfaces.rest.resources;

public record RoleDefinitionResource(
    Long id,
    String name,
    String description,
    int permissionsCount
) {}
