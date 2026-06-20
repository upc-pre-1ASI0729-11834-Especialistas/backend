package com.test.backend.automation.interfaces.rest.transform;

import com.test.backend.automation.domain.model.commands.CreateAutomationRuleCommand;
import com.test.backend.automation.interfaces.rest.resources.CreateAutomationRuleResource;

public class CreateAutomationRuleCommandFromResourceAssembler {
    public static CreateAutomationRuleCommand toCommandFromResource(CreateAutomationRuleResource resource) {
        if (resource == null) return null;
        return new CreateAutomationRuleCommand(
            resource.name(),
            resource.active() != null ? resource.active() : true,
            resource.lastTriggered(),
            resource.triggerMetric(),
            resource.triggerOperator(),
            resource.triggerValue(),
            resource.triggerUnit(),
            resource.triggerDuration(),
            resource.scope(),
            resource.specificLabId(),
            resource.actions(),
            resource.executionLimitMins(),
            resource.autoResolve() != null ? resource.autoResolve() : false
        );
    }
}
