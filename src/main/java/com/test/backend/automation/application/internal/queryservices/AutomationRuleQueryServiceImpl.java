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

    public AutomationRuleQueryServiceImpl(AutomationRuleRepository automationRuleRepository) {
        this.automationRuleRepository = automationRuleRepository;
    }

    @Override
    public List<AutomationRule> handle(GetAllAutomationRulesQuery query) {
        return automationRuleRepository.findAll();
    }

    @Override
    public Optional<AutomationRule> handle(Long id) {
        return automationRuleRepository.findById(id);
    }
}
