package com.test.backend.iam.application.internal.eventhandlers;

import com.test.backend.automation.domain.model.aggregates.UserProfile;
import com.test.backend.automation.domain.model.aggregates.GeneralSetting;
import com.test.backend.automation.domain.model.aggregates.SensorConfiguration;
import com.test.backend.automation.domain.model.aggregates.EquipmentThreshold;
import com.test.backend.automation.domain.model.entities.NotificationPreference;
import com.test.backend.automation.domain.model.entities.UserWorkspaceAccess;
import com.test.backend.automation.domain.model.entities.LabUserAccess;
import com.test.backend.automation.infrastructure.persistence.jpa.repositories.*;
import com.test.backend.labs.domain.model.aggregates.Laboratory;
import com.test.backend.labs.domain.model.entities.LabMetric;
import com.test.backend.labs.domain.model.entities.LabMetricSubscription;
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
import com.test.backend.iam.domain.model.commands.SignUpCommand;
import com.test.backend.iam.domain.model.queries.GetUserByEmailQuery;
import com.test.backend.iam.domain.model.valueobjects.Roles;
import com.test.backend.iam.domain.services.UserCommandService;
import com.test.backend.iam.domain.services.UserQueryService;
import com.test.backend.labs.domain.model.aggregates.Workspace;
import com.test.backend.labs.infrastructure.persistence.jpa.repositories.WorkspaceRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Temporary Demo Seeder.
 * Populates the database with rich historical metrics, multiple labs, active alerts,
 * and configures a default Operator user (admin@safelab.com) with lab access.
 * Delete or comment out the @Component annotation to disable seeding.
 */
@Component
@Order(2)
public class TemporarySeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(TemporarySeeder.class);

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

    public TemporarySeeder(UserCommandService userCommandService,
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

    @Override
    @Transactional
    public void run(String... args) {
        logger.info("Initializing Temporary Demo Data Seeder...");

        // 1. Seed Default Operator User (admin@safelab.com / admin123)
        String userEmail = "admin@safelab.com";
        var existingUser = userQueryService.handle(new GetUserByEmailQuery(userEmail));

        if (existingUser.isEmpty()) {
            var signUpCommand = new SignUpCommand(
                    userEmail,
                    "admin123",
                    "SafeLab Operator",
                    List.of(Roles.ROLE_USER) // Regular User role
            );
            userCommandService.handle(signUpCommand);
            logger.info("IAM User created: {} / admin123", userEmail);
        } else {
            logger.info("IAM User already exists. Skipping IAM registration.");
        }

        // 2. Fetch or Create Workspace
        var workspaceOpt = workspaceRepository.findAll().stream()
                .filter(w -> w.getName().contains("SafeLab") || w.getName().contains("Workspace"))
                .findFirst();

        Workspace tempWorkspace;
        if (workspaceOpt.isEmpty()) {
            var ws = new Workspace();
            ws.setName("SafeLab Base Workspace");
            ws.setCode("base-operator-workspace");
            tempWorkspace = workspaceRepository.save(ws);
            logger.info("Workspace created: {}", tempWorkspace.getName());
        } else {
            tempWorkspace = workspaceOpt.get();
        }
        final Workspace workspace = tempWorkspace;

        // 3. Fetch or Create Automation Role
        var operatorRole = automationRoleRepository.findByName("Operator")
                .orElseGet(() -> {
                    var role = new com.test.backend.automation.domain.model.entities.Role();
                    role.setName("Operator");
                    role.setDescription("Operator role");
                    return automationRoleRepository.save(role);
                });

        // 4. Bind Profile with Operator role and access tier
        UserProfile profile = userProfileRepository.findByEmail(userEmail)
                .orElseGet(() -> {
                    var p = new UserProfile();
                    p.setEmail(userEmail);
                    p.setWorkspaceId(workspace.getId());
                    return p;
                });

        profile.setRole(operatorRole);
        profile.setFullName("SafeLab Operator");
        profile.setProfessionalTitle("Lab Operator");
        profile.setEmployeeId("EMP-00001");
        profile.setSystemState("Active");
        profile.setAccessTier("Tier 2");
        profile.setDefaultStartShift("08:00 AM");
        profile.setShiftDuration("8 Hours");
        profile.setAutoGenerateShiftReport(false);
        profile.setWorkspaceId(workspace.getId());
        profile = userProfileRepository.save(profile);
        logger.info("UserProfile synchronized and set to Operator role.");

        // 5. Workspace Access Setup
        final UserProfile finalProfile = profile;
        final Workspace finalWorkspace = workspace;
        userWorkspaceAccessRepository.findByUserProfileEmailAndWorkspaceId(userEmail, workspace.getId())
                .orElseGet(() -> {
                    var access = new UserWorkspaceAccess();
                    access.setUserProfile(finalProfile);
                    access.setWorkspace(finalWorkspace);
                    access.setRole(operatorRole);
                    return userWorkspaceAccessRepository.save(access);
                });

        // 6. Seed General Settings
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

        // 7. Seed Notification Preferences
        if (notificationPreferenceRepository.findByUserProfileId(profile.getId()).isEmpty()) {
            notificationPreferenceRepository.save(new NotificationPreference(profile, "Email", true, "All", "All notifications via email"));
            notificationPreferenceRepository.save(new NotificationPreference(profile, "In-App", true, "All", "Realtime alerts in the application"));
            notificationPreferenceRepository.save(new NotificationPreference(profile, "SMS", true, "Critical Only", "Urgent alerts on mobile devices"));
            logger.info("Notification preferences seeded.");
        }

        // 8. Seed Laboratories, Metric Subscriptions, Equipment, Sensors, and Readings
        if (laboratoryRepository.findByWorkspaceId(workspace.getId()).isEmpty()) {
            logger.info("Seeding laboratories and sensor infrastructure...");

            var tempType = metricTypeRepository.findByKey("temperature").orElse(null);
            var humidityType = metricTypeRepository.findByKey("humidity").orElse(null);
            var co2Type = metricTypeRepository.findByKey("co2").orElse(null);
            var pressureType = metricTypeRepository.findByKey("pressure").orElse(null);
            var airQualityType = metricTypeRepository.findByKey("air_quality").orElse(null);
            var vibrationType = metricTypeRepository.findByKey("vibration").orElse(null);

            // Lab 1: Bio-Safety Lab 04
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
            lab1.setWorkspace(workspace);
            lab1 = laboratoryRepository.save(lab1);

            if (tempType != null) lab1.getMetricSubscriptions().add(new LabMetricSubscription(lab1, tempType, 2.0, 8.0, true));
            if (humidityType != null) lab1.getMetricSubscriptions().add(new LabMetricSubscription(lab1, humidityType, 30.0, 60.0, true));
            if (airQualityType != null) lab1.getMetricSubscriptions().add(new LabMetricSubscription(lab1, airQualityType, 0.0, 50.0, true));
            lab1 = laboratoryRepository.save(lab1);

            // Lab 2: Chemical Synthesis Lab
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
            lab2.setWorkspace(workspace);
            lab2 = laboratoryRepository.save(lab2);

            if (tempType != null) lab2.getMetricSubscriptions().add(new LabMetricSubscription(lab2, tempType, 15.0, 25.0, true));
            if (co2Type != null) lab2.getMetricSubscriptions().add(new LabMetricSubscription(lab2, co2Type, 400.0, 1000.0, true));
            if (humidityType != null) lab2.getMetricSubscriptions().add(new LabMetricSubscription(lab2, humidityType, 30.0, 65.0, true));
            lab2 = laboratoryRepository.save(lab2);

            // Lab 3: Cryogenic Storage Room
            var lab3 = new Laboratory();
            lab3.setName("Cryogenic Storage Room");
            lab3.setType("Cryo Storage");
            lab3.setStatus("Critical");
            lab3.setBuilding("Building A");
            lab3.setFloor("Floor 1");
            lab3.setLabCode("LAB-CRYO-03");
            lab3.setRoomNumber("105");
            lab3.setDescription("Ultra-low temperature biological sample repository");
            lab3.setOverallStatus("Critical");
            lab3.setActive(true);
            lab3.setLive(true);
            lab3.setNextMaintenance(LocalDate.now().plusDays(60));
            lab3.setMaintenanceDaysLeft(60);
            lab3.setNotificationPreferences(new NotificationPreferences(true, true, true, true));
            lab3.setWorkspace(workspace);
            lab3 = laboratoryRepository.save(lab3);

            if (tempType != null) lab3.getMetricSubscriptions().add(new LabMetricSubscription(lab3, tempType, -200.0, -180.0, true));
            if (pressureType != null) lab3.getMetricSubscriptions().add(new LabMetricSubscription(lab3, pressureType, 950.0, 1050.0, true));
            lab3 = laboratoryRepository.save(lab3);

            // Lab 4: Analytical Testing Unit
            var lab4 = new Laboratory();
            lab4.setName("Analytical Testing Unit");
            lab4.setType("Analytical Labs");
            lab4.setStatus("Operational");
            lab4.setBuilding("Building C");
            lab4.setFloor("Floor 3");
            lab4.setLabCode("LAB-ANAL-04");
            lab4.setRoomNumber("310");
            lab4.setDescription("Spectrometry and chromatography services");
            lab4.setOverallStatus("Operational");
            lab4.setActive(true);
            lab4.setLive(true);
            lab4.setNextMaintenance(LocalDate.now().plusDays(90));
            lab4.setMaintenanceDaysLeft(90);
            lab4.setNotificationPreferences(new NotificationPreferences(true, false, false, false));
            lab4.setWorkspace(workspace);
            lab4 = laboratoryRepository.save(lab4);

            if (tempType != null) lab4.getMetricSubscriptions().add(new LabMetricSubscription(lab4, tempType, 18.0, 24.0, true));
            if (humidityType != null) lab4.getMetricSubscriptions().add(new LabMetricSubscription(lab4, humidityType, 25.0, 55.0, true));
            lab4 = laboratoryRepository.save(lab4);

            // 9. Grant profile explicit access to all laboratories
            profile.getLabAccesses().clear();
            profile = userProfileRepository.save(profile);

            List<Laboratory> allLabs = List.of(lab1, lab2, lab3, lab4);
            for (var lab : allLabs) {
                var labAccess = new LabUserAccess();
                labAccess.setUserProfile(profile);
                labAccess.setLaboratory(lab);
                profile.getLabAccesses().add(labAccess);
            }
            profile = userProfileRepository.save(profile);
            logger.info("Assigned LabUserAccess to profile for all 4 laboratories.");

            // 10. Seed Equipment Thresholds
            var eq1 = equipmentThresholdRepository.save(new EquipmentThreshold(lab1, "ULT Freezer F-07", "biotech", -86.0, -75.0, -78.0, "°C", -80.2, "normal"));
            var eq2 = equipmentThresholdRepository.save(new EquipmentThreshold(lab1, "Refrigerator B2", "biotech", 2.0, 8.0, 7.0, "°C", 8.5, "critical")); // breach!
            var eq3 = equipmentThresholdRepository.save(new EquipmentThreshold(lab2, "CO2 Incubator CM-01", "biotech", 400.0, 1200.0, 1000.0, "ppm", 1050.0, "warning")); // warning!
            var eq4 = equipmentThresholdRepository.save(new EquipmentThreshold(lab2, "Reactor R-02", "industrial", 0.0, 3.0, 2.0, "mm/s", 3.5, "warning")); // vibration warning!
            var eq5 = equipmentThresholdRepository.save(new EquipmentThreshold(lab3, "LN2 Tank Cryo-01", "biotech", -200.0, -180.0, -185.0, "°C", -170.0, "critical")); // critical breach!
            var eq6 = equipmentThresholdRepository.save(new EquipmentThreshold(lab4, "GC-MS Column Oven", "analytical", 30.0, 350.0, 300.0, "°C", 150.0, "normal"));
            logger.info("Equipment thresholds seeded.");

            // 11. Seed Sensors
            var sensor1 = new SensorConfiguration();
            sensor1.setSensorName("Sensor T-01");
            sensor1.setType("temperature");
            sensor1.setUnit("MAC-T01-REF");
            sensor1.setActive(true);
            sensor1.setStatus("ONLINE");
            sensor1.setCalibrationDate(LocalDate.now().minusMonths(3));
            sensor1.setLastConnected(LocalDateTime.now().minusMinutes(2));
            sensor1.setLaboratory(lab1);
            sensor1.setEquipment(eq2);
            sensor1 = sensorConfigurationRepository.save(sensor1);

            var sensor2 = new SensorConfiguration();
            sensor2.setSensorName("Sensor H-02");
            sensor2.setType("humidity");
            sensor2.setUnit("MAC-H02-AMB");
            sensor2.setActive(true);
            sensor2.setStatus("ONLINE");
            sensor2.setCalibrationDate(LocalDate.now().minusMonths(4));
            sensor2.setLastConnected(LocalDateTime.now().minusMinutes(5));
            sensor2.setLaboratory(lab1);
            sensor2.setEquipment(null);
            sensor2 = sensorConfigurationRepository.save(sensor2);

            var sensor3 = new SensorConfiguration();
            sensor3.setSensorName("Sensor C-03");
            sensor3.setType("co2");
            sensor3.setUnit("MAC-C03-INC");
            sensor3.setActive(true);
            sensor3.setStatus("ONLINE");
            sensor3.setCalibrationDate(LocalDate.now().minusMonths(1));
            sensor3.setLastConnected(LocalDateTime.now().minusMinutes(1));
            sensor3.setLaboratory(lab2);
            sensor3.setEquipment(eq3);
            sensor3 = sensorConfigurationRepository.save(sensor3);

            var sensor4 = new SensorConfiguration();
            sensor4.setSensorName("Sensor T-04");
            sensor4.setType("temperature");
            sensor4.setUnit("MAC-T04-CRYO");
            sensor4.setActive(true);
            sensor4.setStatus("ONLINE");
            sensor4.setCalibrationDate(LocalDate.now().minusMonths(2));
            sensor4.setLastConnected(LocalDateTime.now().minusMinutes(3));
            sensor4.setLaboratory(lab3);
            sensor4.setEquipment(eq5);
            sensor4 = sensorConfigurationRepository.save(sensor4);

            var sensor5 = new SensorConfiguration();
            sensor5.setSensorName("Sensor P-05");
            sensor5.setType("pressure");
            sensor5.setUnit("MAC-P05-PRES");
            sensor5.setActive(true);
            sensor5.setStatus("ONLINE");
            sensor5.setCalibrationDate(LocalDate.now().minusMonths(6));
            sensor5.setLastConnected(LocalDateTime.now().minusMinutes(10));
            sensor5.setLaboratory(lab3);
            sensor5.setEquipment(null);
            sensor5 = sensorConfigurationRepository.save(sensor5);

            var sensor6 = new SensorConfiguration();
            sensor6.setSensorName("Sensor A-07");
            sensor6.setType("air_quality");
            sensor6.setUnit("MAC-A07-AIR");
            sensor6.setActive(true);
            sensor6.setStatus("ONLINE");
            sensor6.setCalibrationDate(LocalDate.now().minusMonths(5));
            sensor6.setLastConnected(LocalDateTime.now().minusMinutes(7));
            sensor6.setLaboratory(lab2);
            sensor6.setEquipment(null);
            sensor6 = sensorConfigurationRepository.save(sensor6);

            var sensor7 = new SensorConfiguration();
            sensor7.setSensorName("Sensor V-08");
            sensor7.setType("vibration");
            sensor7.setUnit("MAC-V08-VIB");
            sensor7.setActive(true);
            sensor7.setStatus("ONLINE");
            sensor7.setCalibrationDate(LocalDate.now().minusMonths(1));
            sensor7.setLastConnected(LocalDateTime.now().minusMinutes(4));
            sensor7.setLaboratory(lab2);
            sensor7.setEquipment(eq4);
            sensor7 = sensorConfigurationRepository.save(sensor7);

            var sensor8 = new SensorConfiguration();
            sensor8.setSensorName("Sensor T-06");
            sensor8.setType("temperature");
            sensor8.setUnit("MAC-T06-OFFLINE");
            sensor8.setActive(true);
            sensor8.setStatus("OFFLINE");
            sensor8.setCalibrationDate(LocalDate.now().minusMonths(8));
            sensor8.setLastConnected(LocalDateTime.now().minusDays(2));
            sensor8.setLaboratory(lab4);
            sensor8.setEquipment(eq6);
            sensor8 = sensorConfigurationRepository.save(sensor8);

            logger.info("Sensors configured and seeded.");

            // 11.5. Seed Real-time Lab Metrics (required for frontend dashboard and detail view)
            logger.info("Seeding real-time laboratory metrics...");

            // Lab 1 Metrics
            var lm1 = new LabMetric();
            lm1.setLaboratory(lab1);
            lm1.setName("temperature");
            lm1.setValue("8.50");
            lm1.setUnit("°C");
            lm1.setStatus("CRITICAL");
            lm1.setIcon("device_thermostat");
            lm1.setSparkline("4.8,4.9,5.0,5.2,5.5,5.8,7.0,8.5");
            lm1.setThreshold(8.0);
            lm1.setObjectType("Refrigerator B2");

            var lm2 = new LabMetric();
            lm2.setLaboratory(lab1);
            lm2.setName("humidity");
            lm2.setValue("45.20");
            lm2.setUnit("%");
            lm2.setStatus("NORMAL");
            lm2.setIcon("water_drop");
            lm2.setSparkline("43.5,44.0,44.5,45.0,45.2");
            lm2.setThreshold(null);
            lm2.setObjectType("Ambient");

            var lm3 = new LabMetric();
            lm3.setLaboratory(lab1);
            lm3.setName("air_quality");
            lm3.setValue("22.00");
            lm3.setUnit("AQI");
            lm3.setStatus("NORMAL");
            lm3.setIcon("air");
            lm3.setSparkline("20.0,21.0,22.0");
            lm3.setThreshold(null);
            lm3.setObjectType("Ambient");

            lab1.getMetrics().addAll(List.of(lm1, lm2, lm3));
            laboratoryRepository.save(lab1);

            // Lab 2 Metrics
            var lm4 = new LabMetric();
            lm4.setLaboratory(lab2);
            lm4.setName("temperature");
            lm4.setValue("21.30");
            lm4.setUnit("°C");
            lm4.setStatus("NORMAL");
            lm4.setIcon("device_thermostat");
            lm4.setSparkline("20.5,21.0,21.3");
            lm4.setThreshold(null);
            lm4.setObjectType("Ambient");

            var lm5 = new LabMetric();
            lm5.setLaboratory(lab2);
            lm5.setName("co2");
            lm5.setValue("1050.00");
            lm5.setUnit("ppm");
            lm5.setStatus("WARNING");
            lm5.setIcon("co2");
            lm5.setSparkline("550.0,600.0,750.0,900.0,1050.0");
            lm5.setThreshold(1000.0);
            lm5.setObjectType("CO2 Incubator CM-01");

            var lm6 = new LabMetric();
            lm6.setLaboratory(lab2);
            lm6.setName("vibration");
            lm6.setValue("3.50");
            lm6.setUnit("mm/s");
            lm6.setStatus("WARNING");
            lm6.setIcon("graphic_eq");
            lm6.setSparkline("1.1,1.2,1.3,3.5");
            lm6.setThreshold(3.0);
            lm6.setObjectType("Reactor R-02");

            var lm7 = new LabMetric();
            lm7.setLaboratory(lab2);
            lm7.setName("humidity");
            lm7.setValue("48.50");
            lm7.setUnit("%");
            lm7.setStatus("NORMAL");
            lm7.setIcon("water_drop");
            lm7.setSparkline("46.0,47.2,48.5");
            lm7.setThreshold(null);
            lm7.setObjectType("Ambient");

            lab2.getMetrics().addAll(List.of(lm4, lm5, lm6, lm7));
            laboratoryRepository.save(lab2);

            // Lab 3 Metrics
            var lm8 = new LabMetric();
            lm8.setLaboratory(lab3);
            lm8.setName("temperature");
            lm8.setValue("-170.00");
            lm8.setUnit("°C");
            lm8.setStatus("CRITICAL");
            lm8.setIcon("device_thermostat");
            lm8.setSparkline("-192.0,-190.0,-185.0,-180.0,-170.0");
            lm8.setThreshold(-180.0);
            lm8.setObjectType("LN2 Tank Cryo-01");

            var lm9 = new LabMetric();
            lm9.setLaboratory(lab3);
            lm9.setName("pressure");
            lm9.setValue("1012.00");
            lm9.setUnit("hPa");
            lm9.setStatus("NORMAL");
            lm9.setIcon("compress");
            lm9.setSparkline("1008.0,1010.0,1012.0");
            lm9.setThreshold(null);
            lm9.setObjectType("Ambient");

            lab3.getMetrics().addAll(List.of(lm8, lm9));
            laboratoryRepository.save(lab3);

            // Lab 4 Metrics
            var lm10 = new LabMetric();
            lm10.setLaboratory(lab4);
            lm10.setName("temperature");
            lm10.setValue("21.80");
            lm10.setUnit("°C");
            lm10.setStatus("NORMAL");
            lm10.setIcon("device_thermostat");
            lm10.setSparkline("21.5,21.7,21.8");
            lm10.setThreshold(null);
            lm10.setObjectType("GC-MS Column Oven");

            var lm11 = new LabMetric();
            lm11.setLaboratory(lab4);
            lm11.setName("humidity");
            lm11.setValue("40.20");
            lm11.setUnit("%");
            lm11.setStatus("NORMAL");
            lm11.setIcon("water_drop");
            lm11.setSparkline("39.5,40.0,40.2");
            lm11.setThreshold(null);
            lm11.setObjectType("Ambient");

            lab4.getMetrics().addAll(List.of(lm10, lm11));
            laboratoryRepository.save(lab4);

            // 12. Seed rich historical metrics readings (last 48 hours, 1-hour intervals)
            var now = LocalDateTime.now();
            List<SensorReading> readingsToSave = new ArrayList<>();
            logger.info("Generating telemetry trend data points...");

            for (int i = 48; i >= 0; i--) {
                var recordedAt = now.minusHours(i);

                // Sensor 1: Temperature T-01 (linked to Refrigerator B2 in Lab 1)
                // Limit: 2.0 to 8.0. Normal ~ 4.5. Critical spike recently (last 3 hours)
                double val1 = 4.5 + Math.sin(i * 0.4) * 1.5;
                if (i <= 2) {
                    val1 = 8.5; // Trigger critical alarm
                }
                if (tempType != null) {
                    readingsToSave.add(new SensorReading(sensor1, lab1, tempType, val1, recordedAt));
                }

                // Sensor 2: Humidity H-02 (ambient in Lab 1)
                // Limit: 30 to 60. Normal ~ 45
                double val2 = 45.0 + Math.sin(i * 0.3) * 8.0;
                if (humidityType != null) {
                    readingsToSave.add(new SensorReading(sensor2, lab1, humidityType, val2, recordedAt));
                }

                // Sensor 3: CO2 C-03 (CO2 Incubator in Lab 2)
                // Limit: 400 to 1000. Normal ~ 600. Warning spike recently (last 4 hours)
                double val3 = 600.0 + Math.sin(i * 0.2) * 150.0;
                if (i <= 4) {
                    val3 = 1050.0; // Trigger warning alarm
                }
                if (co2Type != null) {
                    readingsToSave.add(new SensorReading(sensor3, lab2, co2Type, val3, recordedAt));
                }

                // Sensor 4: LN2 Temperature T-04 (LN2 Tank in Lab 3)
                // Limit: -200 to -180. Normal ~ -190. Critical spike (last 3 hours) to -170.0 (warmer than -180 is breach)
                double val4 = -190.0 + Math.sin(i * 0.3) * 5.0;
                if (i <= 3) {
                    val4 = -170.0; // Trigger critical alarm
                }
                if (tempType != null) {
                    readingsToSave.add(new SensorReading(sensor4, lab3, tempType, val4, recordedAt));
                }

                // Sensor 5: Pressure P-05 (ambient in Lab 3)
                // Limit: 950 to 1050. Normal ~ 1013
                double val5 = 1013.0 + Math.sin(i * 0.1) * 12.0;
                if (pressureType != null) {
                    readingsToSave.add(new SensorReading(sensor5, lab3, pressureType, val5, recordedAt));
                }

                // Sensor 6: Air Quality A-07 (ambient in Lab 2)
                // Limit: 0 to 50. Normal ~ 35
                double val6 = 35.0 + Math.cos(i * 0.45) * 10.0;
                if (airQualityType != null) {
                    readingsToSave.add(new SensorReading(sensor6, lab2, airQualityType, val6, recordedAt));
                }

                // Sensor 7: Vibration V-08 (Reactor in Lab 2)
                // Limit: 0 to 3.0. Normal ~ 1.2. Warning spike (last 6 hours) to 3.5
                double val7 = 1.2 + Math.abs(Math.sin(i * 0.5)) * 0.8;
                if (i <= 6) {
                    val7 = 3.5; // Trigger warning alert
                }
                if (vibrationType != null) {
                    readingsToSave.add(new SensorReading(sensor7, lab2, vibrationType, val7, recordedAt));
                }
            }
            sensorReadingRepository.saveAll(readingsToSave);
            logger.info("Saved {} telemetry trend data points.", readingsToSave.size());

            // 13. Seed Alerts
            logger.info("Seeding active and resolved incidents/alerts...");
            // Alert 1: Temperature breach in Refrigerator B2
            var alert1 = new Alert();
            alert1.setLaboratory(lab1);
            alert1.setSensorConfiguration(sensor1);
            alert1.setTitle("Temperature Threshold Exceeded");
            alert1.setDescription("Sensor T-01 registered 8.5°C in Refrigerator B2, exceeding the maximum limit of 8.0°C.");
            alert1.setSeverity("CRITICAL");
            alert1.setStatus("ACTIVE");
            alert1.setLabName(lab1.getName());
            alert1.setTimeAgo("2 hours ago");
            alert1 = alertRepository.save(alert1);

            alert1.getMetrics().addAll(List.of(
                new AlertMetric(alert1, "currentValue", "8.5°C"),
                new AlertMetric(alert1, "threshold", "8.0°C"),
                new AlertMetric(alert1, "exceededBy", "0.5°C"),
                new AlertMetric(alert1, "sensorType", "NTC Thermistor"),
                new AlertMetric(alert1, "lastCalibration", LocalDate.now().minusMonths(3).toString()),
                new AlertMetric(alert1, "signalStrength", "98%"),
                new AlertMetric(alert1, "networkStatus", "ONLINE"),
                new AlertMetric(alert1, "automationRuleName", "Cooling Boost Regulation"),
                new AlertMetric(alert1, "automationRuleStatus", "Running"),
                new AlertMetric(alert1, "automationRuleDesc", "Increases cooling flow when temp limit breaches.")
            ));
            alertRepository.save(alert1);

            // Alert 2: LN2 Temperature critical breach
            var alert2 = new Alert();
            alert2.setLaboratory(lab3);
            alert2.setSensorConfiguration(sensor4);
            alert2.setTitle("Cryo LN2 Temperature Critical Alert");
            alert2.setDescription("Sensor T-04 registered -170.0°C in LN2 Tank Cryo-01, warmer than the critical limit of -180.0°C.");
            alert2.setSeverity("CRITICAL");
            alert2.setStatus("ACTIVE");
            alert2.setLabName(lab3.getName());
            alert2.setTimeAgo("3 hours ago");
            alert2 = alertRepository.save(alert2);

            alert2.getMetrics().addAll(List.of(
                new AlertMetric(alert2, "currentValue", "-170.0°C"),
                new AlertMetric(alert2, "threshold", "-180.0°C"),
                new AlertMetric(alert2, "exceededBy", "10.0°C"),
                new AlertMetric(alert2, "sensorType", "Cryogenic RTD Probe"),
                new AlertMetric(alert2, "lastCalibration", LocalDate.now().minusMonths(2).toString()),
                new AlertMetric(alert2, "networkStatus", "ONLINE")
            ));
            alertRepository.save(alert2);

            // Alert 3: CO2 Warning Level
            var alert3 = new Alert();
            alert3.setLaboratory(lab2);
            alert3.setSensorConfiguration(sensor3);
            alert3.setTitle("CO2 Level Warning");
            alert3.setDescription("Sensor C-03 registered 1050 ppm, warning threshold is 1000 ppm.");
            alert3.setSeverity("WARNING");
            alert3.setStatus("ACTIVE");
            alert3.setLabName(lab2.getName());
            alert3.setTimeAgo("4 hours ago");
            alert3 = alertRepository.save(alert3);

            alert3.getMetrics().addAll(List.of(
                new AlertMetric(alert3, "currentValue", "1050 ppm"),
                new AlertMetric(alert3, "threshold", "1000 ppm"),
                new AlertMetric(alert3, "exceededBy", "50 ppm"),
                new AlertMetric(alert3, "sensorType", "NIDR CO2 Sensor")
            ));
            alertRepository.save(alert3);

            // Alert 4: Vibration Warning
            var alert4 = new Alert();
            alert4.setLaboratory(lab2);
            alert4.setSensorConfiguration(sensor7);
            alert4.setTitle("Reactor Vibration Spike");
            alert4.setDescription("Sensor V-08 registered 3.5 mm/s, warning threshold is 3.0 mm/s.");
            alert4.setSeverity("WARNING");
            alert4.setStatus("ACTIVE");
            alert4.setLabName(lab2.getName());
            alert4.setTimeAgo("6 hours ago");
            alert4 = alertRepository.save(alert4);

            alert4.getMetrics().addAll(List.of(
                new AlertMetric(alert4, "currentValue", "3.5 mm/s"),
                new AlertMetric(alert4, "threshold", "3.0 mm/s"),
                new AlertMetric(alert4, "exceededBy", "0.5 mm/s")
            ));
            alertRepository.save(alert4);

            // Alert 5: Resolved Alert
            var alert5 = new Alert();
            alert5.setLaboratory(lab1);
            alert5.setSensorConfiguration(sensor2);
            alert5.setTitle("Humidity Levels Restored");
            alert5.setDescription("Sensor H-02 registered 62.0% (Limit: 60.0%) but returned to normal range (42.5%).");
            alert5.setSeverity("WARNING");
            alert5.setStatus("RESOLVED");
            alert5.setLabName(lab1.getName());
            alert5.setTimeAgo("1 day ago");
            alert5 = alertRepository.save(alert5);

            alert5.getMetrics().addAll(List.of(
                new AlertMetric(alert5, "currentValue", "42.5%"),
                new AlertMetric(alert5, "exceededValue", "62.0%"),
                new AlertMetric(alert5, "threshold", "60.0%")
            ));
            alertRepository.save(alert5);

            logger.info("Active and resolved alerts seeded successfully.");

            // 14. Seed History Records (Audit Trail)
            logger.info("Seeding history audit trail records...");
            historyRecordRepository.save(new HistoryRecord(lab1, "Temperature Critical Alarm", "Sensor T-01 registered 8.5°C (Limit: 8.0°C) in Refrigerator B2.", LocalDateTime.now().minusHours(2), "incident", "Critical", "Active"));
            historyRecordRepository.save(new HistoryRecord(lab1, "Automation rule triggered", "Automation: Cooling Boost Regulation activated for zone Bio-Safety Lab 04.", LocalDateTime.now().minusHours(2).plusMinutes(1), "automation", "Info", "Active"));
            historyRecordRepository.save(new HistoryRecord(lab3, "Cryo LN2 Critical Alert", "Sensor T-04 registered -170.0°C in LN2 Tank Cryo-01.", LocalDateTime.now().minusHours(3), "incident", "Critical", "Active"));
            historyRecordRepository.save(new HistoryRecord(lab2, "CO2 Warning Active", "CO2 level exceeded warning threshold (1050 ppm).", LocalDateTime.now().minusHours(4), "incident", "Warning", "Active"));
            historyRecordRepository.save(new HistoryRecord(lab2, "Vibration Spike Detected", "Vibration level exceeded warning limit on Reactor R-02 (3.5 mm/s).", LocalDateTime.now().minusHours(6), "incident", "Warning", "Active"));
            historyRecordRepository.save(new HistoryRecord(lab1, "Sensor H-02 Warning Resolved", "Humidity levels returned to normal range (42.5%).", LocalDateTime.now().minusDays(1), "incident", "Info", "Resolved"));
            historyRecordRepository.save(new HistoryRecord(lab1, "Manual safety audit logged", "Technician validated secondary cooling pressure gauge. Value: 3.2 bar (Normal).", LocalDateTime.now().minusHours(12), "observation", "Info", "Normal"));
            historyRecordRepository.save(new HistoryRecord(lab4, "HPLC Column Calibrated", "Analytical GC-MS column successfully calibrated by supervisor.", LocalDateTime.now().minusDays(1).minusHours(4), "maintenance", "Info", "Completed"));
            historyRecordRepository.save(new HistoryRecord(lab3, "Pressure check completed", "Cryo room safety valve inspection successfully completed.", LocalDateTime.now().minusDays(2), "maintenance", "Info", "Completed"));
            historyRecordRepository.save(new HistoryRecord(lab2, "Workspace access updated", "Assigned Operator role access permissions to SafeLab Operator.", LocalDateTime.now().minusDays(2).minusHours(1), "system", "Info", "Completed"));
            logger.info("History records seeded successfully.");
        }
    }
}
