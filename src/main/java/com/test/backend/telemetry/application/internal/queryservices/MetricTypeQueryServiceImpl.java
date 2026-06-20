package com.test.backend.telemetry.application.internal.queryservices;

import com.test.backend.telemetry.domain.model.aggregates.MetricType;
import com.test.backend.telemetry.domain.model.queries.GetAllMetricTypesQuery;
import com.test.backend.telemetry.domain.model.queries.GetActiveMetricTypesQuery;
import com.test.backend.telemetry.domain.model.queries.GetMetricTypeByKeyQuery;
import com.test.backend.telemetry.domain.services.MetricTypeQueryService;
import com.test.backend.telemetry.infrastructure.persistence.jpa.repositories.MetricTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MetricTypeQueryServiceImpl implements MetricTypeQueryService {

    private final MetricTypeRepository metricTypeRepository;

    public MetricTypeQueryServiceImpl(MetricTypeRepository metricTypeRepository) {
        this.metricTypeRepository = metricTypeRepository;
    }

    @Override
    public List<MetricType> handle(GetAllMetricTypesQuery query) {
        return metricTypeRepository.findAll();
    }

    @Override
    public List<MetricType> handle(GetActiveMetricTypesQuery query) {
        return metricTypeRepository.findByActiveTrue();
    }

    @Override
    public Optional<MetricType> handle(GetMetricTypeByKeyQuery query) {
        return metricTypeRepository.findByKey(query.key());
    }
}
