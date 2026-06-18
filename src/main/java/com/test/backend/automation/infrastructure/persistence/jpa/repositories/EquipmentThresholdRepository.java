package com.test.backend.automation.infrastructure.persistence.jpa.repositories;

import com.test.backend.automation.domain.model.aggregates.EquipmentThreshold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentThresholdRepository extends JpaRepository<EquipmentThreshold, Long> {
    List<EquipmentThreshold> findByLaboratoryId(Long laboratoryId);
}
