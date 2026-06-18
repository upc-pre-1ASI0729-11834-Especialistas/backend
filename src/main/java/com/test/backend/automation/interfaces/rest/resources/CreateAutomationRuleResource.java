package com.test.backend.automation.interfaces.rest.resources;

import java.util.Date;
import java.util.List;

public record CreateAutomationRuleResource(
    String name,
    Boolean active,
    Date lastTriggered,
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
