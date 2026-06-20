package com.test.backend.automation.interfaces.rest.resources;

import java.util.List;

public record UpdateAutomationRuleResource(
    String name,
    Boolean active,
    java.time.LocalDateTime lastTriggered,
    String triggerMetric,
    String triggerOperator,
    Double triggerValue,
    String triggerUnit,
    Integer triggerDuration,
    String scope,
    Long specificLabId,
    List<String> actions,
    Integer executionLimitMins,
    Boolean autoResolve
) {}
