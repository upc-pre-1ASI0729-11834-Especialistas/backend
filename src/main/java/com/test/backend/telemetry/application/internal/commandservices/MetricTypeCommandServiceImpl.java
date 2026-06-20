package com.test.backend.telemetry.application.internal.commandservices;

import com.test.backend.telemetry.domain.model.aggregates.MetricType;
import com.test.backend.telemetry.domain.model.commands.CreateMetricTypeCommand;
import com.test.backend.telemetry.domain.model.commands.UpdateMetricTypeCommand;
import com.test.backend.telemetry.domain.model.commands.DeleteMetricTypeCommand;
import com.test.backend.telemetry.domain.services.MetricTypeCommandService;
import com.test.backend.telemetry.infrastructure.persistence.jpa.repositories.MetricTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class MetricTypeCommandServiceImpl implements MetricTypeCommandService {

    private final MetricTypeRepository metricTypeRepository;

    public MetricTypeCommandServiceImpl(MetricTypeRepository metricTypeRepository) {
        this.metricTypeRepository = metricTypeRepository;
    }

    @Override
    @Transactional
    public Optional<MetricType> handle(CreateMetricTypeCommand command) {
        if (metricTypeRepository.existsByKey(command.key())) {
            throw new IllegalArgumentException("MetricType with key '" + command.key() + "' already exists");
        }
        var metricType = new MetricType(command);
        metricTypeRepository.save(metricType);
        return Optional.of(metricType);
    }

    @Override
    @Transactional
    public Optional<MetricType> handle(UpdateMetricTypeCommand command) {
        var result = metricTypeRepository.findById(command.id());
        if (result.isEmpty()) return Optional.empty();

        var metricType = result.get();
        // Check for key uniqueness if key changed
        if (!metricType.getKey().equals(command.key()) && metricTypeRepository.existsByKey(command.key())) {
            throw new IllegalArgumentException("MetricType with key '" + command.key() + "' already exists");
        }

        metricType.updateFrom(command);
        metricTypeRepository.save(metricType);
        return Optional.of(metricType);
    }

    @Override
    @Transactional
    public void handle(DeleteMetricTypeCommand command) {
        var result = metricTypeRepository.findById(command.id());
        if (result.isEmpty()) {
            throw new IllegalArgumentException("MetricType not found");
        }
        // Soft delete: deactivate instead of removing
        var metricType = result.get();
        metricType.setActive(false);
        metricTypeRepository.save(metricType);
    }
}
