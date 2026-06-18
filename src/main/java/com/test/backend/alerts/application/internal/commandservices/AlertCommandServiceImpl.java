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

    public AlertCommandServiceImpl(AlertRepository alertRepository, LaboratoryRepository laboratoryRepository) {
        this.alertRepository = alertRepository;
        this.laboratoryRepository = laboratoryRepository;
    }

    @Override
    @Transactional
    public Optional<Alert> handle(CreateAlertCommand command) {
        Laboratory laboratory = null;
        if (command.laboratoryId() != null) {
            var labOpt = laboratoryRepository.findById(command.laboratoryId());
            if (labOpt.isPresent()) {
                laboratory = labOpt.get();
            }
        }
        var alert = new Alert(command, laboratory);
        alertRepository.save(alert);
        return Optional.of(alert);
    }

    @Override
    @Transactional
    public Optional<Alert> handle(UpdateAlertCommand command) {
        var alertOpt = alertRepository.findById(command.id());
        if (alertOpt.isEmpty()) return Optional.empty();

        var alert = alertOpt.get();
        Laboratory laboratory = null;
        if (command.laboratoryId() != null) {
            var labOpt = laboratoryRepository.findById(command.laboratoryId());
            if (labOpt.isPresent()) {
                laboratory = labOpt.get();
            }
        } else if (alert.getLaboratory() != null) {
            laboratory = alert.getLaboratory();
        }

        alert.updateFrom(command, laboratory);
        alertRepository.save(alert);
        return Optional.of(alert);
    }

    @Override
    @Transactional
    public void handle(DeleteAlertCommand command) {
        if (!alertRepository.existsById(command.id())) {
            throw new IllegalArgumentException("Alert not found");
        }
        alertRepository.deleteById(command.id());
    }
}
