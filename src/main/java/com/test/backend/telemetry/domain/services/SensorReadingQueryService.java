package com.test.backend.telemetry.domain.services;

import com.test.backend.telemetry.domain.model.aggregates.SensorReading;
import com.test.backend.telemetry.domain.model.queries.GetAllSensorReadingsQuery;
import com.test.backend.telemetry.domain.model.queries.GetSensorReadingByIdQuery;

import java.util.List;
import java.util.Optional;

public interface SensorReadingQueryService {
    List<SensorReading> handle(GetAllSensorReadingsQuery query);
    Optional<SensorReading> handle(GetSensorReadingByIdQuery query);
}
