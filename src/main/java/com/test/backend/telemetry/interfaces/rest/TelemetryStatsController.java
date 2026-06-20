package com.test.backend.telemetry.interfaces.rest;

import com.test.backend.alerts.infrastructure.persistence.jpa.repositories.AlertRepository;
import com.test.backend.labs.infrastructure.persistence.jpa.repositories.LaboratoryRepository;
import com.test.backend.shared.application.CurrentWorkspaceService;
import com.test.backend.telemetry.interfaces.rest.resources.DashboardStatsResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Telemetry Stats", description = "Telemetry Dashboard Stats API")
@RestController
@RequestMapping("/api/v1/telemetry/stats")
public class TelemetryStatsController {

    private static final Logger logger = LoggerFactory.getLogger(TelemetryStatsController.class);

    private final LaboratoryRepository laboratoryRepository;
    private final AlertRepository alertRepository;
    private final CurrentWorkspaceService currentWorkspaceService;

    public TelemetryStatsController(LaboratoryRepository laboratoryRepository, AlertRepository alertRepository, CurrentWorkspaceService currentWorkspaceService) {
        this.laboratoryRepository = laboratoryRepository;
        this.alertRepository = alertRepository;
        this.currentWorkspaceService = currentWorkspaceService;
    }

    @GetMapping
    @Operation(summary = "Get telemetry dashboard stats")
    public ResponseEntity<List<DashboardStatsResource>> getStats() {
        var profileOpt = currentWorkspaceService.getCurrentUserProfile();
        if (profileOpt.isEmpty()) {
            var emptyStats = new DashboardStatsResource(1L, 0, 0, 100, 0);
            return ResponseEntity.ok(List.of(emptyStats));
        }
        var profile = profileOpt.get();
        Long workspaceId = profile.getWorkspaceId();

        var laboratories = laboratoryRepository.findByWorkspaceId(workspaceId);
        var alerts = alertRepository.findByLaboratoryWorkspaceId(workspaceId);

        if (profile.getRole() == null || !"Administrator".equalsIgnoreCase(profile.getRole().getName())) {
            var allowedLabIds = profile.getLabAccesses().stream()
                    .map(access -> access.getLaboratory().getId())
                    .toList();
            laboratories = laboratories.stream()
                    .filter(l -> allowedLabIds.contains(l.getId()))
                    .toList();
            alerts = alerts.stream()
                    .filter(a -> a.getLaboratory() != null && allowedLabIds.contains(a.getLaboratory().getId()))
                    .toList();
        }

        int totalLaboratories = laboratories.size();
        int activeAlerts = (int) alerts.stream()
                .filter(a -> a.getStatus() == null || !a.getStatus().equalsIgnoreCase("RESOLVED"))
                .count();
        
        long criticalCount = laboratories.stream()
                .filter(l -> l.getOverallStatus() != null && l.getOverallStatus().equalsIgnoreCase("CRITICAL"))
                .count();
        
        int systemsHealth = 100;
        if (totalLaboratories > 0) {
            systemsHealth = 100 - (int) ((criticalCount * 100) / totalLaboratories);
            if (systemsHealth < 0) systemsHealth = 0;
        }

        int upcomingMaintenance = (int) laboratories.stream()
                .filter(l -> l.getMaintenanceDaysLeft() != null && l.getMaintenanceDaysLeft() <= 7)
                .count();

        var stats = new DashboardStatsResource(
                1L,
                totalLaboratories,
                activeAlerts,
                systemsHealth,
                upcomingMaintenance
        );

        return ResponseEntity.ok(List.of(stats));
    }
}
