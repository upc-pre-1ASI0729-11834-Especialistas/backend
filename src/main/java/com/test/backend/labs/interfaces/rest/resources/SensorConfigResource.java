package com.test.backend.labs.interfaces.rest.resources;

public record SensorConfigResource(
    boolean temperature,
    boolean airQuality,
    boolean aiDetection,
    boolean ventilation,
    boolean airConditioning,
    boolean vibration,
    boolean lighting
) {}
