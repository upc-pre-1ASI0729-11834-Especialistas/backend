package com.test.backend.telemetry.interfaces.rest;

import com.test.backend.alerts.domain.model.commands.DeleteAlertCommand;
import com.test.backend.alerts.domain.model.queries.GetAllAlertsQuery;
import com.test.backend.alerts.domain.model.queries.GetAlertByIdQuery;
import com.test.backend.alerts.domain.services.AlertCommandService;
import com.test.backend.alerts.domain.services.AlertQueryService;
import com.test.backend.alerts.interfaces.rest.resources.CreateAlertResource;
import com.test.backend.alerts.interfaces.rest.resources.UpdateAlertResource;
import com.test.backend.alerts.interfaces.rest.transform.CreateAlertCommandFromResourceAssembler;
import com.test.backend.alerts.interfaces.rest.transform.UpdateAlertCommandFromResourceAssembler;
import com.test.backend.telemetry.interfaces.rest.resources.TelemetryAlertResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Telemetry Alerts", description = "Telemetry Alerts View API")
@RestController
@RequestMapping("/api/v1/telemetry/alerts")
public class TelemetryAlertController {

    private final AlertCommandService alertCommandService;
    private final AlertQueryService alertQueryService;

    public TelemetryAlertController(AlertCommandService alertCommandService, AlertQueryService alertQueryService) {
        this.alertCommandService = alertCommandService;
        this.alertQueryService = alertQueryService;
    }

    @GetMapping
    @Operation(summary = "Get all telemetry alerts")
    public ResponseEntity<List<TelemetryAlertResource>> getAllAlerts() {
        var query = new GetAllAlertsQuery();
        var alerts = alertQueryService.handle(query);
        var resources = alerts.stream()
                .map(alert -> new TelemetryAlertResource(
                        alert.getId(),
                        alert.getLaboratory() != null ? alert.getLaboratory().getName() : alert.getLabName(),
                        alert.getTitle(),
                        alert.getDescription(),
                        alert.getSeverity(),
                        alert.getTimeAgo()
                ))
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get telemetry alert by ID")
    public ResponseEntity<TelemetryAlertResource> getAlertById(@PathVariable Long id) {
        var query = new GetAlertByIdQuery(id);
        var result = alertQueryService.handle(query);
        return result.map(alert -> ResponseEntity.ok(new TelemetryAlertResource(
                        alert.getId(),
                        alert.getLaboratory() != null ? alert.getLaboratory().getName() : alert.getLabName(),
                        alert.getTitle(),
                        alert.getDescription(),
                        alert.getSeverity(),
                        alert.getTimeAgo()
                )))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create a new telemetry alert")
    public ResponseEntity<TelemetryAlertResource> createAlert(@RequestBody CreateAlertResource resource) {
        var command = CreateAlertCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = alertCommandService.handle(command);
        return result.map(alert -> new ResponseEntity<>(new TelemetryAlertResource(
                        alert.getId(),
                        alert.getLaboratory() != null ? alert.getLaboratory().getName() : alert.getLabName(),
                        alert.getTitle(),
                        alert.getDescription(),
                        alert.getSeverity(),
                        alert.getTimeAgo()
                ), HttpStatus.CREATED))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing telemetry alert")
    public ResponseEntity<TelemetryAlertResource> updateAlert(@PathVariable Long id, @RequestBody UpdateAlertResource resource) {
        var command = UpdateAlertCommandFromResourceAssembler.toCommandFromResource(id, resource);
        var result = alertCommandService.handle(command);
        return result.map(alert -> ResponseEntity.ok(new TelemetryAlertResource(
                        alert.getId(),
                        alert.getLaboratory() != null ? alert.getLaboratory().getName() : alert.getLabName(),
                        alert.getTitle(),
                        alert.getDescription(),
                        alert.getSeverity(),
                        alert.getTimeAgo()
                )))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete telemetry alert by ID")
    public ResponseEntity<?> deleteAlert(@PathVariable Long id) {
        var command = new DeleteAlertCommand(id);
        try {
            alertCommandService.handle(command);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
