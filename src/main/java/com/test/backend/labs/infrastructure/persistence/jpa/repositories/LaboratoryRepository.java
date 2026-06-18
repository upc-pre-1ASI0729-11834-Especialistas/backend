package com.test.backend.labs.infrastructure.persistence.jpa.repositories;

import com.test.backend.labs.domain.model.aggregates.Laboratory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LaboratoryRepository extends JpaRepository<Laboratory, Long> {
    Optional<Laboratory> findByLabCode(String labCode);
}
