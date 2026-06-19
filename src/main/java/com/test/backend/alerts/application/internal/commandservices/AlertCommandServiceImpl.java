package com.test.backend.alerts.application.internal.commandservices;

import com.test.backend.alerts.domain.model.aggregates.Alert;
import com.test.backend.alerts.domain.model.commands.CreateAlertCommand;
import com.test.backend.alerts.domain.model.commands.UpdateAlertCommand;
import com.test.backend.alerts.domain.model.commands.DeleteAlertCommand;
import com.test.backend.alerts.domain.services.AlertCommandService;
import com.test.backend.alerts.infrastructure.persistence.jpa.repositories.AlertRepository;
import com.test.backend.labs.domain.model.aggregates.Laboratory;
import com.test.backend.labs.infrastructure.persistence.jpa.repositories.LaboratoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AlertCommandServiceImpl implements AlertCommandService {

    private final AlertRepository alertRepository;
    private final LaboratoryRepository laboratoryRepository;
    private final com.test.backend.shared.application.CurrentWorkspaceService currentWorkspaceService;

    public AlertCommandServiceImpl(AlertRepository alertRepository,
                                   LaboratoryRepository laboratoryRepository,
                                   com.test.backend.shared.application.CurrentWorkspaceService currentWorkspaceService) {
        this.alertRepository = alertRepository;
        this.laboratoryRepository = laboratoryRepository;
        this.currentWorkspaceService = currentWorkspaceService;
    }

    @Override
    @Transactional
    public Optional<Alert> handle(CreateAlertCommand command) {
        Long workspaceId = currentWorkspaceService.getCurrentWorkspaceId()
                .orElseThrow(() -> new IllegalStateException("User does not have an active workspace"));

        Laboratory laboratory = null;
        if (command.laboratoryId() != null) {
            laboratory = laboratoryRepository.findByIdAndWorkspaceId(command.laboratoryId(), workspaceId)
                    .orElseThrow(() -> new IllegalArgumentException("Laboratory not found in this workspace"));
        }
        var alert = new Alert(command, laboratory);
        alertRepository.save(alert);
        return Optional.of(alert);
    }

    @Override
    @Transactional
    public Optional<Alert> handle(UpdateAlertCommand command) {
        Long workspaceId = currentWorkspaceService.getCurrentWorkspaceId()
                .orElseThrow(() -> new IllegalStateException("User does not have an active workspace"));

        var alertOpt = alertRepository.findByIdAndLaboratoryWorkspaceId(command.id(), workspaceId);
        if (alertOpt.isEmpty()) return Optional.empty();

        var alert = alertOpt.get();
        Laboratory laboratory = null;
        if (command.laboratoryId() != null) {
            laboratory = laboratoryRepository.findByIdAndWorkspaceId(command.laboratoryId(), workspaceId)
                    .orElseThrow(() -> new IllegalArgumentException("Laboratory not found in this workspace"));
        } else if (alert.getLaboratory() != null) {
            laboratory = alert.getLaboratory();
        }

        alert.updateFrom(command, laboratory);
        alertRepository.save(alert);

        if (laboratory != null && "RESOLVED".equalsIgnoreCase(alert.getStatus())) {
            boolean hasOtherActiveAlerts = alertRepository.findByLaboratoryId(laboratory.getId()).stream()
                    .anyMatch(a -> !"RESOLVED".equalsIgnoreCase(a.getStatus()) && !a.getId().equals(alert.getId()));
            if (!hasOtherActiveAlerts) {
                laboratory.setOverallStatus("OPERATIONAL");
                laboratory.setStatus("Operational");
                if (laboratory.getMetrics() != null) {
                    for (var metric : laboratory.getMetrics()) {
                        metric.setStatus("NORMAL");
                    }
                }
                laboratoryRepository.save(laboratory);
            }
        }

        return Optional.of(alert);
    }

    @Override
    @Transactional
    public void handle(DeleteAlertCommand command) {
        Long workspaceId = currentWorkspaceService.getCurrentWorkspaceId()
                .orElseThrow(() -> new IllegalStateException("User does not have an active workspace"));

        var alert = alertRepository.findByIdAndLaboratoryWorkspaceId(command.id(), workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found in this workspace"));

        alertRepository.delete(alert);
    }
}
