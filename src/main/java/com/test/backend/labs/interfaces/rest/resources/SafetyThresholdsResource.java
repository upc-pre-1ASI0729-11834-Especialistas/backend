package com.test.backend.labs.interfaces.rest.resources;

public record SafetyThresholdsResource(
    Double temperatureMin,
    Double temperatureMax,
    Integer maxCo2Ppm,
    String gasSensitivity,
    Double maxVibrationLevel,
    String alertEscalation
) {}
