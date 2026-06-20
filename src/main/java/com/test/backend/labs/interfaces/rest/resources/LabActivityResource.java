package com.test.backend.labs.interfaces.rest.resources;

public record LabActivityResource(
    Long id,
    String title,
    String description,
    String timestamp,
    String icon
) {}
