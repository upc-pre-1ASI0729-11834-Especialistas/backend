package com.test.backend.labs.interfaces.rest.transform;

import com.test.backend.labs.domain.model.commands.CreateLaboratoryCommand;
import com.test.backend.labs.domain.model.valueobjets.NotificationPreferences;
import com.test.backend.labs.domain.model.valueobjets.SafetyThresholds;
import com.test.backend.labs.domain.model.valueobjets.SensorConfig;
import com.test.backend.labs.interfaces.rest.resources.CreateLaboratoryResource;

public class CreateLaboratoryCommandFromResourceAssembler {
    public static CreateLaboratoryCommand toCommandFromResource(CreateLaboratoryResource resource) {
        if (resource == null) return null;

        SensorConfig sensorConfig = new SensorConfig();
        if (resource.sensors() != null) {
            sensorConfig.setTemperature(resource.sensors().temperature());
            sensorConfig.setAirQuality(resource.sensors().airQuality());
            sensorConfig.setAiDetection(resource.sensors().aiDetection());
            sensorConfig.setVentilation(resource.sensors().ventilation());
            sensorConfig.setAirConditioning(resource.sensors().airConditioning());
            sensorConfig.setVibration(resource.sensors().vibration());
            sensorConfig.setLighting(resource.sensors().lighting());
        }

        SafetyThresholds safetyThresholds = new SafetyThresholds();
        if (resource.thresholds() != null) {
            safetyThresholds.setTempMin(resource.thresholds().temperatureMin());
            safetyThresholds.setTempMax(resource.thresholds().temperatureMax());
            safetyThresholds.setMaxCo2Ppm(resource.thresholds().maxCo2Ppm());
            safetyThresholds.setGasSensitivity(resource.thresholds().gasSensitivity());
            safetyThresholds.setMaxVibration(resource.thresholds().maxVibrationLevel());
            safetyThresholds.setAlertEscalation(resource.thresholds().alertEscalation());
        }

        NotificationPreferences notificationPreferences = new NotificationPreferences();
        if (resource.notifications() != null) {
            notificationPreferences.setEmail(resource.notifications().email());
            notificationPreferences.setSms(resource.notifications().sms());
            notificationPreferences.setPush(resource.notifications().push());
            notificationPreferences.setCriticalOnly(resource.notifications().criticalOnly());
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
            sensorConfig,
            safetyThresholds,
            notificationPreferences
        );
    }
}
