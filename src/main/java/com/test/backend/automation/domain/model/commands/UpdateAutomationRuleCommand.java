package com.test.backend.automation.domain.model.commands;

import java.util.List;

public record UpdateAutomationRuleCommand(
    Long id,
    String name,
    boolean active,
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
    boolean autoResolve
) {}
