package com.test.backend.labs.interfaces.rest.transform;

import com.test.backend.labs.domain.model.aggregates.Laboratory;
import com.test.backend.labs.domain.model.valueobjets.NotificationPreferences;
import com.test.backend.labs.domain.model.valueobjets.SafetyThresholds;
import com.test.backend.labs.domain.model.valueobjets.SensorConfig;
import com.test.backend.labs.interfaces.rest.resources.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LaboratoryResourceFromEntityAssembler {
    public static LaboratoryResource toResourceFromEntity(Laboratory entity) {
        if (entity == null) return null;

        // Map metrics
        List<LabMetricResource> metrics = entity.getMetrics() == null ? new ArrayList<>() :
            entity.getMetrics().stream()
                .map(m -> new LabMetricResource(
                    m.getName(),
                    m.getValue(),
                    m.getUnit(),
                    m.getStatus(),
                    m.getIcon(),
                    parseSparkline(m.getSparkline()),
                    m.getThreshold(),
                    m.getObjectType()
                ))
                .collect(Collectors.toList());

        // Map alerts
        List<LabAlertResource> alerts = entity.getAlerts() == null ? new ArrayList<>() :
            entity.getAlerts().stream()
                .map(a -> new LabAlertResource(
                    a.getId(),
                    a.getTitle(),
                    a.getSource(),
                    a.getTimeAgo(),
                    a.getSeverity()
                ))
                .collect(Collectors.toList());

        // Map activities
        List<LabActivityResource> activities = entity.getActivities() == null ? new ArrayList<>() :
            entity.getActivities().stream()
                .map(act -> new LabActivityResource(
                    act.getId(),
                    act.getTitle(),
                    act.getDescription(),
                    act.getTimestamp(),
                    act.getIcon()
                ))
                .collect(Collectors.toList());

        // Map schedules
        List<LabScheduleResource> schedules = entity.getSchedules() == null ? new ArrayList<>() :
            entity.getSchedules().stream()
                .map(s -> new LabScheduleResource(
                    s.getId(),
                    s.getName(),
                    s.getTimeRange(),
                    s.isActive(),
                    s.getIcon()
                ))
                .collect(Collectors.toList());

        // Map SensorConfig
        SensorConfigResource sensors = null;
        SensorConfig sc = entity.getSensorConfig();
        if (sc != null) {
            sensors = new SensorConfigResource(
                sc.isTemperature(),
                sc.isAirQuality(),
                sc.isAiDetection(),
                sc.isVentilation(),
                sc.isAirConditioning(),
                sc.isVibration(),
                sc.isLighting()
            );
        }

        // Map SafetyThresholds
        SafetyThresholdsResource thresholds = null;
        SafetyThresholds st = entity.getSafetyThresholds();
        if (st != null) {
            thresholds = new SafetyThresholdsResource(
                st.getTempMin(),
                st.getTempMax(),
                st.getMaxCo2Ppm(),
                st.getGasSensitivity(),
                st.getMaxVibration(),
                st.getAlertEscalation()
            );
        }

        // Map NotificationPreferences
        NotificationPreferencesResource notifications = null;
        NotificationPreferences np = entity.getNotificationPreferences();
        if (np != null) {
            notifications = new NotificationPreferencesResource(
                np.isEmail(),
                np.isSms(),
                np.isPush(),
                np.isCriticalOnly()
            );
        }

        return new LaboratoryResource(
            entity.getId(),
            entity.getName(),
            entity.getType(),
            entity.getStatus(),
            entity.getBuilding(),
            entity.getFloor(),
            entity.getLabCode(),
            entity.getOverallStatus(),
            entity.isActive(),
            entity.getLastUpdate(),
            entity.isLive(),
            entity.getNextMaintenance(),
            entity.getMaintenanceDaysLeft(),
            entity.getRoomNumber(),
            entity.getDescription(),
            metrics,
            alerts,
            activities,
            schedules,
            sensors,
            thresholds,
            notifications
        );
    }

    private static List<Double> parseSparkline(String sparklineStr) {
        if (sparklineStr == null || sparklineStr.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return Arrays.stream(sparklineStr.split(","))
                .map(String::trim)
                .map(Double::parseDouble)
                .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            return new ArrayList<>();
        }
    }
}
