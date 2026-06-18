package com.test.backend.labs.interfaces.rest.transform;

import com.test.backend.labs.domain.model.commands.UpdateLaboratoryCommand;
import com.test.backend.labs.domain.model.valueobjets.NotificationPreferences;
import com.test.backend.labs.interfaces.rest.resources.UpdateLaboratoryResource;

import java.util.ArrayList;
import java.util.List;

public class UpdateLaboratoryCommandFromResourceAssembler {
    public static UpdateLaboratoryCommand toCommandFromResource(Long id, UpdateLaboratoryResource resource) {
        if (resource == null) return null;

        NotificationPreferences notificationPreferences = new NotificationPreferences();
        if (resource.notifications() != null) {
            notificationPreferences.setEmail(resource.notifications().email());
            notificationPreferences.setSms(resource.notifications().sms());
            notificationPreferences.setPush(resource.notifications().push());
            notificationPreferences.setCriticalOnly(resource.notifications().criticalOnly());
        }

        List<UpdateLaboratoryCommand.MetricSubscriptionData> subscriptions = new ArrayList<>();
        if (resource.metricSubscriptions() != null) {
            for (var sub : resource.metricSubscriptions()) {
                subscriptions.add(new UpdateLaboratoryCommand.MetricSubscriptionData(
                    sub.metricTypeId(),
                    sub.minThreshold(),
                    sub.maxThreshold()
                ));
            }
        }

        return new UpdateLaboratoryCommand(
            id,
            resource.name(),
            resource.type(),
            resource.status() != null ? resource.status() : "OPERATIONAL",
            resource.building(),
            resource.floor(),
            resource.labCode(),
            resource.roomNumber(),
            resource.description(),
            resource.overallStatus() != null ? resource.overallStatus() : "OPERATIONAL",
            resource.active() != null ? resource.active() : true,
            resource.isLive() != null ? resource.isLive() : true,
            resource.nextMaintenance() != null ? resource.nextMaintenance() : java.time.LocalDate.now(),
            resource.maintenanceDaysLeft() != null ? resource.maintenanceDaysLeft() : 30,
            subscriptions,
            notificationPreferences
        );
    }
}
