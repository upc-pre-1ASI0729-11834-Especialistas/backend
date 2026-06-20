package com.test.backend.telemetry.interfaces.rest.transform;

import com.test.backend.telemetry.domain.model.aggregates.MetricType;
import com.test.backend.telemetry.interfaces.rest.resources.MetricTypeResource;

public class MetricTypeResourceFromEntityAssembler {
    public static MetricTypeResource toResourceFromEntity(MetricType entity) {
        if (entity == null) return null;
        return new MetricTypeResource(
            entity.getId(),
            entity.getKey(),
            entity.getDisplayName(),
            entity.getUnit(),
            entity.getIcon(),
            entity.getCategory(),
            entity.isActive()
        );
    }
}
