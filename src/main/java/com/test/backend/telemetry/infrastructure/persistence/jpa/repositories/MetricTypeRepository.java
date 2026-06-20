package com.test.backend.telemetry.infrastructure.persistence.jpa.repositories;

import com.test.backend.telemetry.domain.model.aggregates.MetricType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MetricTypeRepository extends JpaRepository<MetricType, Long> {
    Optional<MetricType> findByKey(String key);
    List<MetricType> findByActiveTrue();
    boolean existsByKey(String key);
}
