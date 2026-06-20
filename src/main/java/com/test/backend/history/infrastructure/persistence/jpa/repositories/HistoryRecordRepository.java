package com.test.backend.history.infrastructure.persistence.jpa.repositories;

import com.test.backend.history.domain.model.aggregates.HistoryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryRecordRepository extends JpaRepository<HistoryRecord, Long> {
    List<HistoryRecord> findByLaboratoryId(Long laboratoryId);
    List<HistoryRecord> findByLaboratoryWorkspaceId(Long workspaceId);
    java.util.Optional<HistoryRecord> findByIdAndLaboratoryWorkspaceId(Long id, Long workspaceId);
}
