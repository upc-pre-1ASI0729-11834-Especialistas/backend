package com.test.backend.alerts.interfaces.rest.transform;

import com.test.backend.alerts.domain.model.aggregates.Alert;
import com.test.backend.alerts.interfaces.rest.resources.AlertMetricResource;
import com.test.backend.alerts.interfaces.rest.resources.AlertResource;

import java.util.List;

public class AlertResourceFromEntityAssembler {
    public static AlertResource toResourceFromEntity(Alert entity) {
        if (entity == null) return null;

        String createdAtStr = entity.getCreatedAt() != null 
                ? entity.getCreatedAt().toInstant().toString() 
                : null;

        Long laboratoryId = entity.getLaboratory() != null 
                ? entity.getLaboratory().getId() 
                : null;

        String labName = entity.getLaboratory() != null 
                ? entity.getLaboratory().getName() 
                : entity.getLabName();

        String labLocation = "N/A";
        if (entity.getLaboratory() != null) {
            String building = entity.getLaboratory().getBuilding() != null ? entity.getLaboratory().getBuilding() : "";
            String floor = entity.getLaboratory().getFloor() != null ? entity.getLaboratory().getFloor() : "";
            String roomNumber = entity.getLaboratory().getRoomNumber() != null ? entity.getLaboratory().getRoomNumber() : "";
            labLocation = building + " - Floor " + floor + " - Room " + roomNumber;
        }

        Long sensorId = entity.getSensorConfiguration() != null 
                ? entity.getSensorConfiguration().getId() 
                : null;

        String sensorName = entity.getSensorConfiguration() != null 
                ? entity.getSensorConfiguration().getSensorName() 
                : "N/A";

        String equipmentName = (entity.getSensorConfiguration() != null && entity.getSensorConfiguration().getEquipment() != null)
                ? entity.getSensorConfiguration().getEquipment().getName()
                : null;

        List<AlertMetricResource> metrics = List.of();
        if (entity.getMetrics() != null) {
            metrics = entity.getMetrics().stream()
                    .map(m -> new AlertMetricResource(m.getLabel(), m.getValue()))
                    .toList();
        }

        return new AlertResource(
            entity.getId(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getSeverity(),
            entity.getStatus(),
            createdAtStr,
            laboratoryId,
            labName,
            labLocation,
            sensorId,
            sensorName,
            equipmentName,
            metrics
        );
    }
}
