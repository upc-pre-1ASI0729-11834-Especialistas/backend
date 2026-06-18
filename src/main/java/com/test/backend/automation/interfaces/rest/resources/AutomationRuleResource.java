package com.test.backend.automation.interfaces.rest.resources;

import java.util.Date;
import java.util.List;

public record AutomationRuleResource(
    Long id,
    String name,
    boolean active,
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
    boolean autoResolve
) {}
