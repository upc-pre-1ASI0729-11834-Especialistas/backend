package com.test.backend.telemetry.interfaces.rest.transform;

import com.test.backend.telemetry.domain.model.commands.CreateMetricTypeCommand;
import com.test.backend.telemetry.interfaces.rest.resources.CreateMetricTypeResource;

public class CreateMetricTypeCommandFromResourceAssembler {
    public static CreateMetricTypeCommand toCommandFromResource(CreateMetricTypeResource resource) {
        if (resource == null) return null;
        return new CreateMetricTypeCommand(
            resource.key(),
            resource.displayName(),
            resource.unit(),
            resource.icon(),
            resource.category()
        );
    }
}
