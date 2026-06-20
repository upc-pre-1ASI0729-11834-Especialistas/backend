package com.test.backend.alerts.interfaces.rest.transform;

import com.test.backend.alerts.domain.model.commands.UpdateAlertCommand;
import com.test.backend.alerts.interfaces.rest.resources.UpdateAlertResource;

public class UpdateAlertCommandFromResourceAssembler {
    public static UpdateAlertCommand toCommandFromResource(Long id, UpdateAlertResource resource) {
        if (resource == null) return null;
        return new UpdateAlertCommand(
            id,
            resource.title(),
            resource.description(),
            resource.severity(),
            resource.status(),
            resource.labName(),
            resource.timeAgo(),
            resource.laboratoryId()
        );
    }
}
