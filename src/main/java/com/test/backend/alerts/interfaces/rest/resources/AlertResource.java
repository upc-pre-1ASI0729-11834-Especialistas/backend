package com.test.backend.alerts.interfaces.rest.resources;

import java.util.List;

public record AlertResource(
    Long id,
    String title,
    String description,
    String severity,
    String status,
    String createdAt,
    Long laboratoryId,
    String labName,
    String labLocation,
    Long sensorId,
    String sensorName,
    String equipmentName,
    List<AlertMetricResource> metrics
) {}
