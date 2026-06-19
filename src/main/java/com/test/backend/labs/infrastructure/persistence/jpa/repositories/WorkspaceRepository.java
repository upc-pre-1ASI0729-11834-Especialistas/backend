package com.test.backend.labs.infrastructure.persistence.jpa.repositories;

import com.test.backend.labs.domain.model.aggregates.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
    Optional<Workspace> findByCode(String code);
}
