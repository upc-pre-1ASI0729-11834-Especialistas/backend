package com.test.backend.alerts.application.internal.queryservices;

import com.test.backend.alerts.domain.model.aggregates.Alert;
import com.test.backend.alerts.domain.model.queries.GetAllAlertsQuery;
import com.test.backend.alerts.domain.model.queries.GetAlertByIdQuery;
import com.test.backend.alerts.domain.services.AlertQueryService;
import com.test.backend.alerts.infrastructure.persistence.jpa.repositories.AlertRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AlertQueryServiceImpl implements AlertQueryService {

    private final AlertRepository alertRepository;

    public AlertQueryServiceImpl(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @Override
    public List<Alert> handle(GetAllAlertsQuery query) {
        return alertRepository.findAll();
    }

    @Override
    public Optional<Alert> handle(GetAlertByIdQuery query) {
        return alertRepository.findById(query.id());
    }
}
