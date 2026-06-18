package com.test.backend.labs.interfaces.rest.resources;

public record MetricSubscriptionInputResource(
    Long metricTypeId,
    Double minThreshold,
    Double maxThreshold
) {}
