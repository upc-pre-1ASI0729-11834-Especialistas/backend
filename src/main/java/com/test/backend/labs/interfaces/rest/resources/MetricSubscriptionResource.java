package com.test.backend.labs.interfaces.rest.resources;

import java.util.List;

public record MetricSubscriptionResource(
    Long metricTypeId,
    String metricTypeKey,
    String metricTypeDisplayName,
    String metricTypeUnit,
    String metricTypeIcon,
    String metricTypeCategory,
    Double minThreshold,
    Double maxThreshold,
    boolean active
) {}
