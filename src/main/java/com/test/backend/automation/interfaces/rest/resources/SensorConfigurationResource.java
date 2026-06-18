package com.test.backend.automation.interfaces.rest.resources;

import java.util.Date;

public record SensorConfigurationResource(
    Long id,
    String sensorName,
    String type,
    String unit,
    Date calibrationDate,
    boolean isActive
) {}
