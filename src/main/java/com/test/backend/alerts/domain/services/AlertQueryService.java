package com.test.backend.alerts.domain.services;

import com.test.backend.alerts.domain.model.aggregates.Alert;
import com.test.backend.alerts.domain.model.queries.GetAllAlertsQuery;
import com.test.backend.alerts.domain.model.queries.GetAlertByIdQuery;

import java.util.List;
import java.util.Optional;

public interface AlertQueryService {
    List<Alert> handle(GetAllAlertsQuery query);
    Optional<Alert> handle(GetAlertByIdQuery query);
}
