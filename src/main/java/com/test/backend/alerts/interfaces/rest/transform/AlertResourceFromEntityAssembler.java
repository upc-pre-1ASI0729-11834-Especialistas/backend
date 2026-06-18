package com.test.backend.alerts.interfaces.rest.transform;

import com.test.backend.alerts.domain.model.aggregates.Alert;
import com.test.backend.alerts.interfaces.rest.resources.AlertResource;

public class AlertResourceFromEntityAssembler {
    public static AlertResource toResourceFromEntity(Alert entity) {
        if (entity == null) return null;
        return new AlertResource(
            entity.getId(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getSeverity(),
            entity.getStatus()
        );
    }
}
