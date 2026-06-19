package com.test.backend.alerts.application.internal.queryservices;

import com.test.backend.alerts.domain.model.aggregates.Alert;
import com.test.backend.alerts.domain.model.queries.GetAllAlertsQuery;
import com.test.backend.alerts.domain.model.queries.GetAlertByIdQuery;
import com.test.backend.alerts.domain.services.AlertQueryService;
import com.test.backend.alerts.infrastructure.persistence.jpa.repositories.AlertRepository;
import com.test.backend.shared.application.CurrentWorkspaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AlertQueryServiceImpl implements AlertQueryService {

    private static final Logger logger = LoggerFactory.getLogger(AlertQueryServiceImpl.class);

    private final AlertRepository alertRepository;
    private final CurrentWorkspaceService currentWorkspaceService;

    public AlertQueryServiceImpl(AlertRepository alertRepository,
                                 CurrentWorkspaceService currentWorkspaceService) {
        this.alertRepository = alertRepository;
        this.currentWorkspaceService = currentWorkspaceService;
    }

    @Override
    public List<Alert> handle(GetAllAlertsQuery query) {
        var profileOpt = currentWorkspaceService.getCurrentUserProfile();
        if (profileOpt.isEmpty()) {
            return List.of();
        }
        var profile = profileOpt.get();
        var alerts = alertRepository.findByLaboratoryWorkspaceId(profile.getWorkspaceId());

        if (profile.getRole() != null && "Administrator".equalsIgnoreCase(profile.getRole().getName())) {
            return alerts;
        }

        var allowedLabIds = profile.getLabAccesses().stream()
                .map(access -> access.getLaboratory().getId())
                .toList();

        return alerts.stream()
                .filter(alert -> alert.getLaboratory() != null && allowedLabIds.contains(alert.getLaboratory().getId()))
                .toList();
    }

    @Override
    public Optional<Alert> handle(GetAlertByIdQuery query) {
        var profileOpt = currentWorkspaceService.getCurrentUserProfile();
        if (profileOpt.isEmpty()) {
            return Optional.empty();
        }
        var profile = profileOpt.get();
        var alertOpt = alertRepository.findByIdAndLaboratoryWorkspaceId(query.id(), profile.getWorkspaceId());
        if (alertOpt.isEmpty()) {
            return Optional.empty();
        }
        var alert = alertOpt.get();

        if (profile.getRole() != null && "Administrator".equalsIgnoreCase(profile.getRole().getName())) {
            return Optional.of(alert);
        }

        boolean hasAccess = profile.getLabAccesses().stream()
                .anyMatch(access -> alert.getLaboratory() != null && access.getLaboratory().getId().equals(alert.getLaboratory().getId()));

        return hasAccess ? Optional.of(alert) : Optional.empty();
    }
}
