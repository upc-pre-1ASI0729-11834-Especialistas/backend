package com.test.backend.labs.interfaces.rest.transform;

import com.test.backend.labs.domain.model.commands.CreateLaboratoryCommand;
import com.test.backend.labs.domain.model.valueobjets.NotificationPreferences;
import com.test.backend.labs.interfaces.rest.resources.CreateLaboratoryResource;

import java.util.ArrayList;
import java.util.List;

public class CreateLaboratoryCommandFromResourceAssembler {
    public static CreateLaboratoryCommand toCommandFromResource(CreateLaboratoryResource resource) {
        if (resource == null) return null;

        NotificationPreferences notificationPreferences = new NotificationPreferences();
        if (resource.notifications() != null) {
            notificationPreferences.setEmail(resource.notifications().email());
            notificationPreferences.setSms(resource.notifications().sms());
            notificationPreferences.setPush(resource.notifications().push());
            notificationPreferences.setCriticalOnly(resource.notifications().criticalOnly());
        }

        List<CreateLaboratoryCommand.MetricSubscriptionData> subscriptions = new ArrayList<>();
        if (resource.metricSubscriptions() != null) {
            for (var sub : resource.metricSubscriptions()) {
                subscriptions.add(new CreateLaboratoryCommand.MetricSubscriptionData(
                    sub.metricTypeId(),
                    sub.minThreshold(),
                    sub.maxThreshold()
                ));
            }
        }

        return new CreateLaboratoryCommand(
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
