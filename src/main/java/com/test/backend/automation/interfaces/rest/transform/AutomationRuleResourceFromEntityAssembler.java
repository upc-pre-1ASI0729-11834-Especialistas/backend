package com.test.backend.automation.interfaces.rest.transform;

import com.test.backend.automation.domain.model.aggregates.AutomationRule;
import com.test.backend.automation.interfaces.rest.resources.AutomationRuleResource;

public class AutomationRuleResourceFromEntityAssembler {
    public static AutomationRuleResource toResourceFromEntity(AutomationRule entity) {
        if (entity == null) return null;
        Long labId = entity.getSpecificLab() != null ? entity.getSpecificLab().getId() : null;
        return new AutomationRuleResource(
            entity.getId(),
            entity.getName(),
            entity.isActive(),
            entity.getLastTriggered(),
            entity.getTriggerMetric(),
            entity.getTriggerOperator(),
            entity.getTriggerValue(),
            entity.getTriggerUnit(),
            entity.getTriggerDuration(),
            entity.getScope(),
            labId,
            entity.getActions(),
            entity.getExecutionLimitMins(),
            entity.isAutoResolve()
        );
    }
}
