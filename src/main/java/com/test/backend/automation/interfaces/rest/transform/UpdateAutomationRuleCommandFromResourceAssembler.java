package com.test.backend.automation.interfaces.rest.transform;

import com.test.backend.automation.domain.model.commands.UpdateAutomationRuleCommand;
import com.test.backend.automation.interfaces.rest.resources.UpdateAutomationRuleResource;

public class UpdateAutomationRuleCommandFromResourceAssembler {
    public static UpdateAutomationRuleCommand toCommandFromResource(Long id, UpdateAutomationRuleResource resource) {
        if (resource == null) return null;
        return new UpdateAutomationRuleCommand(
            id,
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
