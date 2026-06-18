package com.test.backend.telemetry.interfaces.rest.transform;

import com.test.backend.telemetry.domain.model.commands.UpdateMetricTypeCommand;
import com.test.backend.telemetry.interfaces.rest.resources.UpdateMetricTypeResource;

public class UpdateMetricTypeCommandFromResourceAssembler {
    public static UpdateMetricTypeCommand toCommandFromResource(Long id, UpdateMetricTypeResource resource) {
        if (resource == null) return null;
        return new UpdateMetricTypeCommand(
            id,
            resource.key(),
            resource.displayName(),
            resource.unit(),
            resource.icon(),
            resource.category(),
            resource.active() != null ? resource.active() : true
        );
    }
}
