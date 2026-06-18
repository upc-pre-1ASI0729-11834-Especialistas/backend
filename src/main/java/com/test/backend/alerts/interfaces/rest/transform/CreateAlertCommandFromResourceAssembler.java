package com.test.backend.alerts.interfaces.rest.transform;

import com.test.backend.alerts.domain.model.commands.CreateAlertCommand;
import com.test.backend.alerts.interfaces.rest.resources.CreateAlertResource;

public class CreateAlertCommandFromResourceAssembler {
    public static CreateAlertCommand toCommandFromResource(CreateAlertResource resource) {
        if (resource == null) return null;
        return new CreateAlertCommand(
            resource.title(),
            resource.description(),
            resource.severity(),
            resource.status() != null ? resource.status() : "Active",
            resource.labName(),
            resource.timeAgo() != null ? resource.timeAgo() : "Just now",
            resource.laboratoryId()
        );
    }
}
