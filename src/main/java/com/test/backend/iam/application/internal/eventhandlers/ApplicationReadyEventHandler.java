package com.test.backend.iam.application.internal.eventhandlers;

import com.test.backend.automation.domain.model.aggregates.UserProfile;
import com.test.backend.automation.domain.model.aggregates.GeneralSetting;
import com.test.backend.automation.domain.model.aggregates.SensorConfiguration;
import com.test.backend.automation.domain.model.aggregates.EquipmentThreshold;
import com.test.backend.automation.domain.model.entities.NotificationPreference;
import com.test.backend.automation.domain.model.entities.UserWorkspaceAccess;
import com.test.backend.automation.infrastructure.persistence.jpa.repositories.*;
import com.test.backend.labs.domain.model.aggregates.Laboratory;
import com.test.backend.labs.domain.model.entities.*;
import com.test.backend.labs.domain.model.valueobjets.NotificationPreferences;
import com.test.backend.labs.infrastructure.persistence.jpa.repositories.LaboratoryRepository;
import com.test.backend.telemetry.domain.model.aggregates.MetricType;
import com.test.backend.telemetry.domain.model.aggregates.SensorReading;
import com.test.backend.telemetry.infrastructure.persistence.jpa.repositories.MetricTypeRepository;
import com.test.backend.telemetry.infrastructure.persistence.jpa.repositories.SensorReadingRepository;
import com.test.backend.alerts.domain.model.aggregates.Alert;
import com.test.backend.alerts.domain.model.entities.AlertMetric;
import com.test.backend.alerts.infrastructure.persistence.jpa.repositories.AlertRepository;
import com.test.backend.history.domain.model.aggregates.HistoryRecord;
import com.test.backend.history.infrastructure.persistence.jpa.repositories.HistoryRecordRepository;
import com.test.backend.iam.domain.model.commands.SeedRolesCommand;
import com.test.backend.iam.domain.model.commands.SignUpCommand;
import com.test.backend.iam.domain.model.queries.GetUserByEmailQuery;
import com.test.backend.iam.domain.model.valueobjects.Roles;
import com.test.backend.iam.domain.services.RoleCommandService;
import com.test.backend.iam.domain.services.UserCommandService;
import com.test.backend.iam.domain.services.UserQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.test.backend.labs.domain.model.aggregates.Workspace;
import com.test.backend.labs.infrastructure.persistence.jpa.repositories.WorkspaceRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class ApplicationReadyEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationReadyEventHandler.class);

    private final RoleCommandService roleCommandService;
    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;
    private final UserProfileRepository userProfileRepository;
    private final RoleRepository automationRoleRepository;
    private final GeneralSettingRepository generalSettingRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final LaboratoryRepository laboratoryRepository;
    private final SensorConfigurationRepository sensorConfigurationRepository;
    private final SensorReadingRepository sensorReadingRepository;
    private final MetricTypeRepository metricTypeRepository;
    private final EquipmentThresholdRepository equipmentThresholdRepository;
    private final AlertRepository alertRepository;
    private final HistoryRecordRepository historyRecordRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserWorkspaceAccessRepository userWorkspaceAccessRepository;

    public ApplicationReadyEventHandler(RoleCommandService roleCommandService,
                                         UserCommandService userCommandService,
                                         UserQueryService userQueryService,
                                         UserProfileRepository userProfileRepository,
                                         RoleRepository automationRoleRepository,
                                         GeneralSettingRepository generalSettingRepository,
                                         NotificationPreferenceRepository notificationPreferenceRepository,
                                         LaboratoryRepository laboratoryRepository,
                                         SensorConfigurationRepository sensorConfigurationRepository,
                                         SensorReadingRepository sensorReadingRepository,
                                         MetricTypeRepository metricTypeRepository,
                                         EquipmentThresholdRepository equipmentThresholdRepository,
                                         AlertRepository alertRepository,
                                         HistoryRecordRepository historyRecordRepository,
                                         WorkspaceRepository workspaceRepository,
                                         UserWorkspaceAccessRepository userWorkspaceAccessRepository) {
        this.roleCommandService = roleCommandService;
        this.userCommandService = userCommandService;
        this.userQueryService = userQueryService;
        this.userProfileRepository = userProfileRepository;
        this.automationRoleRepository = automationRoleRepository;
        this.generalSettingRepository = generalSettingRepository;
        this.notificationPreferenceRepository = notificationPreferenceRepository;
        this.laboratoryRepository = laboratoryRepository;
        this.sensorConfigurationRepository = sensorConfigurationRepository;
        this.sensorReadingRepository = sensorReadingRepository;
        this.metricTypeRepository = metricTypeRepository;
        this.equipmentThresholdRepository = equipmentThresholdRepository;
        this.alertRepository = alertRepository;
        this.historyRecordRepository = historyRecordRepository;
        this.workspaceRepository = workspaceRepository;
        this.userWorkspaceAccessRepository = userWorkspaceAccessRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void on(ApplicationReadyEvent event) {
        logger.info("Application is ready. Starting IAM seeding process...");

        // 1. Seed IAM Roles
        roleCommandService.handle(new SeedRolesCommand());
        logger.info("IAM Roles seeded successfully.");

        // 2. Seed Default Admin
        String adminEmail = "admin@safelab.com";
        var adminUser = userQueryService.handle(new GetUserByEmailQuery(adminEmail));

        if (adminUser.isEmpty()) {
            var signUpCommand = new SignUpCommand(
                    adminEmail,
                    "admin123",
                    "SafeLab Administrator",
                    List.of(Roles.ROLE_ADMIN)
            );
            userCommandService.handle(signUpCommand);
            logger.info("Default admin user created: {} / admin123", adminEmail);
        } else {
            logger.info("Default admin user already exists. Skipping seeding.");
        }

        // 3. Seed Default Admin Profile in Automation Context
        var automationAdminRole = automationRoleRepository.findByName("Administrator")
                .orElseGet(() -> {
                    var role = new com.test.backend.automation.domain.model.entities.Role();
                    role.setName("Administrator");
                    role.setDescription("Administrator role");
                    return automationRoleRepository.save(role);
                });

        UserProfile adminProfile = null;
        var profileOpt = userProfileRepository.findByEmail(adminEmail);
        Workspace adminWorkspace = null;

        if (profileOpt.isEmpty()) {
            adminWorkspace = new Workspace();
            adminWorkspace.setName("SafeLab Base Workspace");
            adminWorkspace.setCode("base-admin-workspace");
            adminWorkspace = workspaceRepository.save(adminWorkspace);

            var profile = new UserProfile();
            profile.setEmail(adminEmail);
            profile.setFullName("SafeLab Administrator");
            profile.setRole(automationAdminRole);
            profile.setProfessionalTitle("Administrator");
            profile.setEmployeeId("EMP-00001");
            profile.setSystemState("Active");
            profile.setAccessTier("Tier 1");
            profile.setDefaultStartShift("08:00 AM");
            profile.setShiftDuration("8 Hours");
            profile.setAutoGenerateShiftReport(false);
            profile.setWorkspaceId(adminWorkspace.getId());
            adminProfile = userProfileRepository.save(profile);
            logger.info("Default user profile created in automation context: {}", adminEmail);
        } else {
            adminProfile = profileOpt.get();
            Long workspaceId = adminProfile.getWorkspaceId();
            adminWorkspace = workspaceRepository.findById(workspaceId)
                    .orElseGet(() -> {
                        var ws = new Workspace();
                        ws.setName("Workspace of SafeLab Administrator");
                        return workspaceRepository.save(ws);
                    });
            adminWorkspace.setName("SafeLab Base Workspace");
            adminWorkspace.setCode("base-admin-workspace");
            adminWorkspace = workspaceRepository.save(adminWorkspace);

            logger.info("Reusing and renaming existing workspace ID: {} for seeding", adminWorkspace.getId());
        }

        if (adminProfile != null && adminWorkspace != null) {
            final UserProfile finalAdminProfile = adminProfile;
            final Workspace finalAdminWorkspace = adminWorkspace;
            userWorkspaceAccessRepository.findByUserProfileEmailAndWorkspaceId(adminEmail, adminWorkspace.getId())
                    .orElseGet(() -> {
                        var access = new UserWorkspaceAccess();
                        access.setUserProfile(finalAdminProfile);
                        access.setWorkspace(finalAdminWorkspace);
                        access.setRole(automationAdminRole);
                        return userWorkspaceAccessRepository.save(access);
                    });
        }

        // 4. Seed General Settings
        if (generalSettingRepository.count() == 0) {
            generalSettingRepository.save(new GeneralSetting("tempWarningLevel", "2c", "alert_thresholds", "Deviation for warning visual state"));
            generalSettingRepository.save(new GeneralSetting("tempCriticalLevel", "5c", "alert_thresholds", "Deviation for critical push notification"));
            generalSettingRepository.save(new GeneralSetting("dailySummary", "false", "reports", "Daily snapshot summary report"));
            generalSettingRepository.save(new GeneralSetting("weeklyReport", "false", "reports", "Weekly trend and compliance report"));
            generalSettingRepository.save(new GeneralSetting("instantSensorAlerts", "true", "notifications", "Instant alert for connection loss"));
            generalSettingRepository.save(new GeneralSetting("coordinatorPhone", "+1 (555) 000-0000", "general", "Coordinator notification phone number"));
            generalSettingRepository.save(new GeneralSetting("quietHoursEnabled", "true", "general", "Silence alerts during quiet hours"));
            generalSettingRepository.save(new GeneralSetting("quietHoursStart", "10:00 PM", "general", "Start of quiet hours"));
            generalSettingRepository.save(new GeneralSetting("quietHoursEnd", "06:00 AM", "general", "End of quiet hours"));
            logger.info("General Settings seeded.");
        }

        // 5. Seed Notification Preferences for Admin
        if (adminProfile != null && notificationPreferenceRepository.findByUserProfileId(adminProfile.getId()).isEmpty()) {
            notificationPreferenceRepository.save(new NotificationPreference(adminProfile, "Email", true, "All", "All notifications via email"));
            notificationPreferenceRepository.save(new NotificationPreference(adminProfile, "In-App", true, "All", "Realtime alerts in the application"));
            notificationPreferenceRepository.save(new NotificationPreference(adminProfile, "SMS", true, "Critical Only", "Urgent alerts on mobile devices"));
            logger.info("Notification preferences seeded for admin.");
        }

        // 6. Seed Laboratories, Metric Subscriptions and Sensors
        if (laboratoryRepository.findByWorkspaceId(adminWorkspace.getId()).isEmpty()) {
            var tempType = metricTypeRepository.findByKey("temperature").orElse(null);
            var humidityType = metricTypeRepository.findByKey("humidity").orElse(null);
            var co2Type = metricTypeRepository.findByKey("co2").orElse(null);

            // Lab 1
            var lab1 = new Laboratory();
            lab1.setName("Bio-Safety Lab 04");
            lab1.setType("Biohazard Level 3");
            lab1.setStatus("Operational");
            lab1.setBuilding("Building A");
            lab1.setFloor("Floor 2");
            lab1.setLabCode("LAB-BSL4-01");
            lab1.setRoomNumber("204");
            lab1.setDescription("High pathogen containment research unit");
            lab1.setOverallStatus("Operational");
            lab1.setActive(true);
            lab1.setLive(true);
            lab1.setNextMaintenance(LocalDate.now().plusDays(45));
            lab1.setMaintenanceDaysLeft(45);
            lab1.setNotificationPreferences(new NotificationPreferences(true, true, false, false));
            lab1.setWorkspace(adminWorkspace);
            lab1 = laboratoryRepository.save(lab1);

            // Subscriptions Lab 1
            if (tempType != null) {
                var sub = new LabMetricSubscription(lab1, tempType, 2.0, 8.0, true);
                lab1.getMetricSubscriptions().add(sub);
            }
            if (humidityType != null) {
                var sub = new LabMetricSubscription(lab1, humidityType, 30.0, 60.0, true);
                lab1.getMetricSubscriptions().add(sub);
            }
            laboratoryRepository.save(lab1);

            // Lab 2
            var lab2 = new Laboratory();
            lab2.setName("Chemical Synthesis Lab");
            lab2.setType("Chemical Synthesis");
            lab2.setStatus("Warning");
            lab2.setBuilding("Building B");
            lab2.setFloor("Floor 1");
            lab2.setLabCode("LAB-CHEM-02");
            lab2.setRoomNumber("102");
            lab2.setDescription("Organic compound development and analysis");
            lab2.setOverallStatus("Warning");
            lab2.setActive(true);
            lab2.setLive(true);
            lab2.setNextMaintenance(LocalDate.now().plusDays(15));
            lab2.setMaintenanceDaysLeft(15);
            lab2.setNotificationPreferences(new NotificationPreferences(true, false, true, false));
            lab2.setWorkspace(adminWorkspace);
            lab2 = laboratoryRepository.save(lab2);

            // Subscriptions Lab 2
            if (tempType != null) {
                var sub = new LabMetricSubscription(lab2, tempType, 15.0, 25.0, true);
                lab2.getMetricSubscriptions().add(sub);
            }
            if (co2Type != null) {
                var sub = new LabMetricSubscription(lab2, co2Type, 400.0, 1000.0, true);
                lab2.getMetricSubscriptions().add(sub);
            }
            laboratoryRepository.save(lab2);

            logger.info("Laboratories seeded successfully.");

            // Seed Sensors
            var sensor1 = new SensorConfiguration();
            sensor1.setSensorName("Sensor T-01");
            sensor1.setType("temperature");
            sensor1.setUnit("°C");
            sensor1.setActive(true);
            sensor1.setStatus("ONLINE");
            sensor1.setCalibrationDate(LocalDate.now().minusMonths(3));
            sensor1.setLastConnected(LocalDateTime.now().minusMinutes(2));
            sensor1.setLaboratory(lab1);
            sensor1 = sensorConfigurationRepository.save(sensor1);

            var sensor2 = new SensorConfiguration();
            sensor2.setSensorName("Sensor H-02");
            sensor2.setType("humidity");
            sensor2.setUnit("%");
            sensor2.setActive(true);
            sensor2.setStatus("ONLINE");
            sensor2.setCalibrationDate(LocalDate.now().minusMonths(4));
            sensor2.setLastConnected(LocalDateTime.now().minusMinutes(5));
            sensor2.setLaboratory(lab1);
            sensorConfigurationRepository.save(sensor2);

            var sensor3 = new SensorConfiguration();
            sensor3.setSensorName("Sensor C-03");
            sensor3.setType("co2");
            sensor3.setUnit("ppm");
            sensor3.setActive(true);
            sensor3.setStatus("ONLINE");
            sensor3.setCalibrationDate(LocalDate.now().minusMonths(1));
            sensor3.setLastConnected(LocalDateTime.now().minusMinutes(1));
            sensor3.setLaboratory(lab2);
            sensorConfigurationRepository.save(sensor3);

            logger.info("Sensors configured and seeded.");

            // Seed telemetry readings (trends)
            if (tempType != null && sensorReadingRepository.findByLaboratoryWorkspaceId(adminWorkspace.getId()).isEmpty()) {
                var now = LocalDateTime.now();
                for (int i = 24; i >= 0; i--) {
                    var recordedAt = now.minusHours(i);
                    // Sembramos valores de temperatura fluctuando entre 4°C y 9°C (Lab 1)
                    double val1 = 5.0 + Math.sin(i * 0.5) * 1.5 + (i == 2 ? 3.5 : 0.0); // El valor i=2 simula una alerta (8.5 °C)
                    sensorReadingRepository.save(new SensorReading(sensor1, lab1, tempType, val1, recordedAt));
                }
                logger.info("Telemetry readings seeded.");
            }

            // 7. Seed Equipment Thresholds
            if (equipmentThresholdRepository.findByLaboratoryWorkspaceId(adminWorkspace.getId()).isEmpty()) {
                equipmentThresholdRepository.save(new EquipmentThreshold(lab1, "ULT Freezer F-07", "kitchen", -86.0, -75.0, -78.0, "°C", -80.2, "normal"));
                equipmentThresholdRepository.save(new EquipmentThreshold(lab1, "Refrigerator B2", "kitchen", 2.0, 8.0, 7.0, "°C", 8.4, "critical")); // breach!
                equipmentThresholdRepository.save(new EquipmentThreshold(lab2, "CO2 Incubator CM-01", "biotech", 400.0, 1200.0, 1000.0, "ppm", 550.0, "normal"));
                logger.info("Equipment thresholds seeded.");
            }

            // 8. Seed Alerts
            if (alertRepository.findByLaboratoryWorkspaceId(adminWorkspace.getId()).isEmpty()) {
                var alert = new Alert();
                alert.setLaboratory(lab1);
                alert.setSensorConfiguration(sensor1);
                alert.setTitle("Temperature Threshold Exceeded");
                alert.setDescription("Sensor T-01 registered 8.4°C in Refrigerator B2, exceeding the maximum limit of 8.0°C.");
                alert.setSeverity("CRITICAL");
                alert.setStatus("ACTIVE");
                alert.setLabName(lab1.getName());
                alert.setTimeAgo("15 mins ago");
                alert = alertRepository.save(alert);

                var metricsList = List.of(
                    new AlertMetric(alert, "currentValue", "8.4°C"),
                    new AlertMetric(alert, "threshold", "8.0°C"),
                    new AlertMetric(alert, "exceededBy", "0.4°C"),
                    new AlertMetric(alert, "sensorType", "NTC Thermistor"),
                    new AlertMetric(alert, "lastCalibration", LocalDate.now().minusMonths(3).toString()),
                    new AlertMetric(alert, "signalStrength", "98%"),
                    new AlertMetric(alert, "networkStatus", "ONLINE"),
                    new AlertMetric(alert, "automationRuleName", "Cooling Boost Regulation"),
                    new AlertMetric(alert, "automationRuleStatus", "Running"),
                    new AlertMetric(alert, "automationRuleDesc", "Increases cooling flow when temp limit breaches.")
                );
                alert.getMetrics().addAll(metricsList);
                alertRepository.save(alert);

                // Alerta activa de advertencia
                var resolvedAlert = new Alert();
                resolvedAlert.setLaboratory(lab2);
                resolvedAlert.setSensorConfiguration(sensor3);
                resolvedAlert.setTitle("CO2 Level Warning");
                resolvedAlert.setDescription("Sensor C-03 registered 1050 ppm, warning threshold was 1000 ppm.");
                resolvedAlert.setSeverity("WARNING");
                resolvedAlert.setStatus("ACTIVE");
                resolvedAlert.setLabName(lab2.getName());
                resolvedAlert.setTimeAgo("2 hours ago");
                resolvedAlert = alertRepository.save(resolvedAlert);

                var rMetrics = List.of(
                    new AlertMetric(resolvedAlert, "currentValue", "1050 ppm"),
                    new AlertMetric(resolvedAlert, "threshold", "1000 ppm"),
                    new AlertMetric(resolvedAlert, "exceededBy", "50 ppm")
                );
                resolvedAlert.getMetrics().addAll(rMetrics);
                alertRepository.save(resolvedAlert);

                logger.info("Incidents and alerts seeded.");
            }

            // 9. Seed History Records
            if (historyRecordRepository.findByLaboratoryWorkspaceId(adminWorkspace.getId()).isEmpty()) {
                historyRecordRepository.save(new HistoryRecord(lab1, "Temperature Critical Alarm", "Sensor T-01 registered 8.4°C (Limit: 8.0°C) in Refrigerator B2.", LocalDateTime.now().minusMinutes(15), "incident", "Critical", "Active"));
                historyRecordRepository.save(new HistoryRecord(lab1, "Automation rule triggered", "Automation: Cooling Boost Regulation activated for zone Bio-Safety Lab 04.", LocalDateTime.now().minusMinutes(14), "automation", "Info", "Active"));
                historyRecordRepository.save(new HistoryRecord(lab2, "CO2 Warning Active", "CO2 level exceeded threshold (1050 ppm).", LocalDateTime.now().minusHours(2), "incident", "Warning", "Active"));
                historyRecordRepository.save(new HistoryRecord(lab1, "Manual safety audit logged", "Technician validated secondary cooling pressure gauge. Value: 3.2 bar (Normal).", LocalDateTime.now().minusHours(5), "observation", "Info", "Normal"));
                logger.info("History and audit trail records seeded.");
            }
        }
    }
}
