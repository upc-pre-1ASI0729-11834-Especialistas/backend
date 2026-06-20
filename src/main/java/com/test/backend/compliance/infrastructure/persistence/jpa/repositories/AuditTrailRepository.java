package com.test.backend.compliance.infrastructure.persistence.jpa.repositories;

import com.test.backend.compliance.domain.model.aggregates.AuditTrail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditTrailRepository extends JpaRepository<AuditTrail, Long> {
    List<AuditTrail> findByTargetId(String targetId);
}
