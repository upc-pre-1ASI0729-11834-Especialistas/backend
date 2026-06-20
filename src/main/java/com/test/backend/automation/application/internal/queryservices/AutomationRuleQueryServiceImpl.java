package com.test.backend.automation.application.internal.queryservices;

import com.test.backend.automation.domain.model.aggregates.AutomationRule;
import com.test.backend.automation.domain.model.queries.GetAllAutomationRulesQuery;
import com.test.backend.automation.domain.services.AutomationRuleQueryService;
import com.test.backend.automation.infrastructure.persistence.jpa.repositories.AutomationRuleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AutomationRuleQueryServiceImpl implements AutomationRuleQueryService {

    private final AutomationRuleRepository automationRuleRepository;
    private final com.test.backend.shared.application.CurrentWorkspaceService currentWorkspaceService;

    public AutomationRuleQueryServiceImpl(AutomationRuleRepository automationRuleRepository,
                                           com.test.backend.shared.application.CurrentWorkspaceService currentWorkspaceService) {
        this.automationRuleRepository = automationRuleRepository;
        this.currentWorkspaceService = currentWorkspaceService;
    }

    @Override
    public List<AutomationRule> handle(GetAllAutomationRulesQuery query) {
        return currentWorkspaceService.getCurrentWorkspaceId()
                .map(automationRuleRepository::findByWorkspaceId)
                .orElse(List.of());
    }

    @Override
    public Optional<AutomationRule> handle(Long id) {
        return currentWorkspaceService.getCurrentWorkspaceId()
                .flatMap(workspaceId -> automationRuleRepository.findByIdAndWorkspaceId(id, workspaceId));
    }
}
