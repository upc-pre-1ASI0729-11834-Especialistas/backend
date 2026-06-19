package com.test.backend.automation.infrastructure.persistence.jpa.repositories;

import com.test.backend.automation.domain.model.aggregates.AutomationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AutomationRuleRepository extends JpaRepository<AutomationRule, Long> {
    List<AutomationRule> findByWorkspaceId(Long workspaceId);
    Optional<AutomationRule> findByIdAndWorkspaceId(Long id, Long workspaceId);
}
