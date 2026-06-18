package com.test.backend.alerts.infrastructure.persistence.jpa.repositories;

import com.test.backend.alerts.domain.model.aggregates.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByLaboratoryId(Long laboratoryId);
}
