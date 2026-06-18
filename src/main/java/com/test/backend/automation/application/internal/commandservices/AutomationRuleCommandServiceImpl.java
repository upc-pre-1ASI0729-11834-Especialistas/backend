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

    public AutomationRuleCommandServiceImpl(AutomationRuleRepository automationRuleRepository,
                                           LaboratoryRepository laboratoryRepository) {
        this.automationRuleRepository = automationRuleRepository;
        this.laboratoryRepository = laboratoryRepository;
    }

    @Override
    @Transactional
    public Optional<AutomationRule> handle(CreateAutomationRuleCommand command) {
        Laboratory lab = null;
        if (command.specificLabId() != null) {
            lab = laboratoryRepository.findById(command.specificLabId()).orElse(null);
        }
        var rule = new AutomationRule(command, lab);
        automationRuleRepository.save(rule);
        return Optional.of(rule);
    }

    @Override
    @Transactional
    public Optional<AutomationRule> handle(UpdateAutomationRuleCommand command) {
        var ruleOpt = automationRuleRepository.findById(command.id());
        if (ruleOpt.isEmpty()) return Optional.empty();

        var rule = ruleOpt.get();
        Laboratory lab = null;
        if (command.specificLabId() != null) {
            lab = laboratoryRepository.findById(command.specificLabId()).orElse(null);
        }

        rule.updateFrom(command, lab);
        automationRuleRepository.save(rule);
        return Optional.of(rule);
    }

    @Override
    @Transactional
    public void handle(DeleteAutomationRuleCommand command) {
        if (!automationRuleRepository.existsById(command.id())) {
            throw new IllegalArgumentException("AutomationRule not found");
        }
        automationRuleRepository.deleteById(command.id());
    }
}
