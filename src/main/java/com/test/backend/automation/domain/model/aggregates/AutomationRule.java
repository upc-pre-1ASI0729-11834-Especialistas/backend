package com.test.backend.automation.domain.model.aggregates;

import com.test.backend.automation.infrastructure.persistence.jpa.converters.StringListConverter;
import com.test.backend.labs.domain.model.aggregates.Laboratory;
import com.test.backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "automation_rules")
public class AutomationRule extends AuditableAbstractAggregateRoot<AutomationRule> {

    @Column(nullable = false)
    private String name;

    private boolean active;

    @Column(name = "last_triggered")
    private java.time.LocalDateTime lastTriggered;

    @Column(name = "trigger_metric")
    private String triggerMetric;

    @Column(name = "trigger_operator")
    private String triggerOperator;

    @Column(name = "trigger_value")
    private Double triggerValue;

    @Column(name = "trigger_unit")
    private String triggerUnit;

    @Column(name = "trigger_duration")
    private Integer triggerDuration;

    private String scope;

    @ManyToOne
    @JoinColumn(name = "specific_lab_id")
    private Laboratory specificLab;

    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> actions;

    @Column(name = "execution_limit_mins")
    private Integer executionLimitMins;

    @Column(name = "auto_resolve")
    private boolean autoResolve;

    @Column(name = "workspace_id", nullable = false)
    private Long workspaceId;

    public AutomationRule(com.test.backend.automation.domain.model.commands.CreateAutomationRuleCommand command, Laboratory specificLab, Long workspaceId) {
        this.name = command.name();
        this.active = command.active();
        this.lastTriggered = command.lastTriggered();
        this.triggerMetric = command.triggerMetric();
        this.triggerOperator = command.triggerOperator();
        this.triggerValue = command.triggerValue();
        this.triggerUnit = command.triggerUnit();
        this.triggerDuration = command.triggerDuration();
        this.scope = command.scope();
        this.specificLab = specificLab;
        this.actions = command.actions();
        this.executionLimitMins = command.executionLimitMins();
        this.autoResolve = command.autoResolve();
        this.workspaceId = workspaceId;
    }

    public AutomationRule updateFrom(com.test.backend.automation.domain.model.commands.UpdateAutomationRuleCommand command, Laboratory specificLab) {
        this.name = command.name();
        this.active = command.active();
        this.lastTriggered = command.lastTriggered();
        this.triggerMetric = command.triggerMetric();
        this.triggerOperator = command.triggerOperator();
        this.triggerValue = command.triggerValue();
        this.triggerUnit = command.triggerUnit();
        this.triggerDuration = command.triggerDuration();
        this.scope = command.scope();
        this.specificLab = specificLab;
        this.actions = command.actions();
        this.executionLimitMins = command.executionLimitMins();
        this.autoResolve = command.autoResolve();
        return this;
    }
}
