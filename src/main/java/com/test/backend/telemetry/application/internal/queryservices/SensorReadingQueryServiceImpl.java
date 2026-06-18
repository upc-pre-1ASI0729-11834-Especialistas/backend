package com.test.backend.telemetry.application.internal.queryservices;

import com.test.backend.telemetry.domain.model.aggregates.SensorReading;
import com.test.backend.telemetry.domain.model.queries.GetAllSensorReadingsQuery;
import com.test.backend.telemetry.domain.model.queries.GetSensorReadingByIdQuery;
import com.test.backend.telemetry.domain.services.SensorReadingQueryService;
import com.test.backend.telemetry.infrastructure.persistence.jpa.repositories.SensorReadingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SensorReadingQueryServiceImpl implements SensorReadingQueryService {

    private final SensorReadingRepository sensorReadingRepository;

    public SensorReadingQueryServiceImpl(SensorReadingRepository sensorReadingRepository) {
        this.sensorReadingRepository = sensorReadingRepository;
    }

    @Override
    public List<SensorReading> handle(GetAllSensorReadingsQuery query) {
        return sensorReadingRepository.findAll();
    }

    @Override
    public Optional<SensorReading> handle(GetSensorReadingByIdQuery query) {
        return sensorReadingRepository.findById(query.id());
    }
}
