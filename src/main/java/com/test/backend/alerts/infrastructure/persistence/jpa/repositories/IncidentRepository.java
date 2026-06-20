package com.test.backend.alerts.infrastructure.persistence.jpa.repositories;

import com.test.backend.alerts.domain.model.aggregates.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findByLaboratoryId(Long laboratoryId);
}
