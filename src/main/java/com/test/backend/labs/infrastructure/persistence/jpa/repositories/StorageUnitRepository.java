package com.test.backend.labs.infrastructure.persistence.jpa.repositories;

import com.test.backend.labs.domain.model.aggregates.StorageUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StorageUnitRepository extends JpaRepository<StorageUnit, Long> {
}
