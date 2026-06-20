package com.test.backend.alerts.domain.services;

import com.test.backend.alerts.domain.model.aggregates.Alert;
import com.test.backend.alerts.domain.model.commands.CreateAlertCommand;
import com.test.backend.alerts.domain.model.commands.UpdateAlertCommand;
import com.test.backend.alerts.domain.model.commands.DeleteAlertCommand;

import java.util.Optional;

public interface AlertCommandService {
    Optional<Alert> handle(CreateAlertCommand command);
    Optional<Alert> handle(UpdateAlertCommand command);
    void handle(DeleteAlertCommand command);
}
