package com.test.backend.compliance.infrastructure.persistence.jpa.repositories;

import com.test.backend.compliance.domain.model.aggregates.ComplianceReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplianceReportRepository extends JpaRepository<ComplianceReport, Long> {
}
