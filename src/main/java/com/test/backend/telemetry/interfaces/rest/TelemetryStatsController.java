package com.test.backend.telemetry.interfaces.rest;

import com.test.backend.alerts.infrastructure.persistence.jpa.repositories.AlertRepository;
import com.test.backend.labs.infrastructure.persistence.jpa.repositories.LaboratoryRepository;
import com.test.backend.telemetry.interfaces.rest.resources.DashboardStatsResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Telemetry Stats", description = "Telemetry Dashboard Stats API")
@RestController
@RequestMapping("/api/v1/telemetry/stats")
public class TelemetryStatsController {

    private final LaboratoryRepository laboratoryRepository;
    private final AlertRepository alertRepository;

    public TelemetryStatsController(LaboratoryRepository laboratoryRepository, AlertRepository alertRepository) {
        this.laboratoryRepository = laboratoryRepository;
        this.alertRepository = alertRepository;
    }

    @GetMapping
    @Operation(summary = "Get telemetry dashboard stats")
    public ResponseEntity<List<DashboardStatsResource>> getStats() {
        int totalLaboratories = (int) laboratoryRepository.count();
        int activeAlerts = (int) alertRepository.findAll().stream()
                .filter(a -> a.getStatus() == null || !a.getStatus().equalsIgnoreCase("RESOLVED"))
                .count();
        
        long criticalCount = laboratoryRepository.findAll().stream()
                .filter(l -> l.getOverallStatus() != null && l.getOverallStatus().equalsIgnoreCase("CRITICAL"))
                .count();
        
        int systemsHealth = 100;
        if (totalLaboratories > 0) {
            systemsHealth = 100 - (int) ((criticalCount * 100) / totalLaboratories);
            if (systemsHealth < 0) systemsHealth = 0;
        }

        int upcomingMaintenance = (int) laboratoryRepository.findAll().stream()
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
