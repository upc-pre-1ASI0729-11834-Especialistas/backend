package com.test.backend.telemetry.infrastructure.persistence.jpa.repositories;

import com.test.backend.telemetry.domain.model.aggregates.SensorReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {
    List<SensorReading> findByLaboratoryId(Long laboratoryId);
    List<SensorReading> findByMetricTypeKey(String metricKey);
    List<SensorReading> findByMetricTypeId(Long metricTypeId);
    List<SensorReading> findByLaboratoryIdAndMetricTypeKey(Long laboratoryId, String metricKey);
}
