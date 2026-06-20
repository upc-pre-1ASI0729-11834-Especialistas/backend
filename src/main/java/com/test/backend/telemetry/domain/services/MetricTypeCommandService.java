package com.test.backend.telemetry.domain.services;

import com.test.backend.telemetry.domain.model.aggregates.MetricType;
import com.test.backend.telemetry.domain.model.commands.CreateMetricTypeCommand;
import com.test.backend.telemetry.domain.model.commands.UpdateMetricTypeCommand;
import com.test.backend.telemetry.domain.model.commands.DeleteMetricTypeCommand;

import java.util.Optional;

public interface MetricTypeCommandService {
    Optional<MetricType> handle(CreateMetricTypeCommand command);
    Optional<MetricType> handle(UpdateMetricTypeCommand command);
    void handle(DeleteMetricTypeCommand command);
}
