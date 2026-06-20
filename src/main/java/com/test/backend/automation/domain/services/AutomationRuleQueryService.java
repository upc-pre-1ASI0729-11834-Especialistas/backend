package com.test.backend.automation.domain.services;

import com.test.backend.automation.domain.model.aggregates.AutomationRule;
import com.test.backend.automation.domain.model.queries.GetAllAutomationRulesQuery;

import java.util.List;
import java.util.Optional;

public interface AutomationRuleQueryService {
    List<AutomationRule> handle(GetAllAutomationRulesQuery query);
    Optional<AutomationRule> handle(Long id);
}
