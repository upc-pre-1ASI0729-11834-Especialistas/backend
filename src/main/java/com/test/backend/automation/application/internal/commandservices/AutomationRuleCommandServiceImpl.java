package com.test.backend.automation.application.internal.commandservices;

import com.test.backend.automation.domain.model.aggregates.AutomationRule;
import com.test.backend.automation.domain.model.commands.CreateAutomationRuleCommand;
import com.test.backend.automation.domain.model.commands.UpdateAutomationRuleCommand;
import com.test.backend.automation.domain.model.commands.DeleteAutomationRuleCommand;
import com.test.backend.automation.domain.services.AutomationRuleCommandService;
import com.test.backend.automation.infrastructure.persistence.jpa.repositories.AutomationRuleRepository;
import com.test.backend.labs.domain.model.aggregates.Laboratory;
import com.test.backend.labs.infrastructure.persistence.jpa.repositories.LaboratoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AutomationRuleCommandServiceImpl implements AutomationRuleCommandService {

    private final AutomationRuleRepository automationRuleRepository;
    private final LaboratoryRepository laboratoryRepository;
    private final com.test.backend.shared.application.CurrentWorkspaceService currentWorkspaceService;

    public AutomationRuleCommandServiceImpl(AutomationRuleRepository automationRuleRepository,
                                            LaboratoryRepository laboratoryRepository,
                                            com.test.backend.shared.application.CurrentWorkspaceService currentWorkspaceService) {
        this.automationRuleRepository = automationRuleRepository;
        this.laboratoryRepository = laboratoryRepository;
        this.currentWorkspaceService = currentWorkspaceService;
    }

    @Override
    @Transactional
    public Optional<AutomationRule> handle(CreateAutomationRuleCommand command) {
        Long workspaceId = currentWorkspaceService.getCurrentWorkspaceId()
                .orElseThrow(() -> new IllegalStateException("User does not have an active workspace"));

        Laboratory lab = null;
        if (command.specificLabId() != null) {
            lab = laboratoryRepository.findByIdAndWorkspaceId(command.specificLabId(), workspaceId)
                    .orElseThrow(() -> new IllegalArgumentException("Laboratory not found in this workspace"));
        }
        var rule = new AutomationRule(command, lab, workspaceId);
        automationRuleRepository.save(rule);
        return Optional.of(rule);
    }

    @Override
    @Transactional
    public Optional<AutomationRule> handle(UpdateAutomationRuleCommand command) {
        Long workspaceId = currentWorkspaceService.getCurrentWorkspaceId()
                .orElseThrow(() -> new IllegalStateException("User does not have an active workspace"));

        var ruleOpt = automationRuleRepository.findByIdAndWorkspaceId(command.id(), workspaceId);
        if (ruleOpt.isEmpty()) return Optional.empty();

        var rule = ruleOpt.get();
        Laboratory lab = null;
        if (command.specificLabId() != null) {
            lab = laboratoryRepository.findByIdAndWorkspaceId(command.specificLabId(), workspaceId)
                    .orElseThrow(() -> new IllegalArgumentException("Laboratory not found in this workspace"));
        }

        rule.updateFrom(command, lab);
        automationRuleRepository.save(rule);
        return Optional.of(rule);
    }

    @Override
    @Transactional
    public void handle(DeleteAutomationRuleCommand command) {
        Long workspaceId = currentWorkspaceService.getCurrentWorkspaceId()
                .orElseThrow(() -> new IllegalStateException("User does not have an active workspace"));

        var rule = automationRuleRepository.findByIdAndWorkspaceId(command.id(), workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("AutomationRule not found in this workspace"));

        automationRuleRepository.delete(rule);
    }
}
