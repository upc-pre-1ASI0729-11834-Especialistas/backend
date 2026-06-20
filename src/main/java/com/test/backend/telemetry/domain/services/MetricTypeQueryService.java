package com.test.backend.telemetry.domain.services;

import com.test.backend.telemetry.domain.model.aggregates.MetricType;
import com.test.backend.telemetry.domain.model.queries.GetAllMetricTypesQuery;
import com.test.backend.telemetry.domain.model.queries.GetActiveMetricTypesQuery;
import com.test.backend.telemetry.domain.model.queries.GetMetricTypeByKeyQuery;

import java.util.List;
import java.util.Optional;

public interface MetricTypeQueryService {
    List<MetricType> handle(GetAllMetricTypesQuery query);
    List<MetricType> handle(GetActiveMetricTypesQuery query);
    Optional<MetricType> handle(GetMetricTypeByKeyQuery query);
}
