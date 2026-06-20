package com.test.backend.automation.domain.services;

import com.test.backend.automation.domain.model.aggregates.AutomationRule;
import com.test.backend.automation.domain.model.commands.CreateAutomationRuleCommand;
import com.test.backend.automation.domain.model.commands.UpdateAutomationRuleCommand;
import com.test.backend.automation.domain.model.commands.DeleteAutomationRuleCommand;

import java.util.Optional;

public interface AutomationRuleCommandService {
    Optional<AutomationRule> handle(CreateAutomationRuleCommand command);
    Optional<AutomationRule> handle(UpdateAutomationRuleCommand command);
    void handle(DeleteAutomationRuleCommand command);
}
