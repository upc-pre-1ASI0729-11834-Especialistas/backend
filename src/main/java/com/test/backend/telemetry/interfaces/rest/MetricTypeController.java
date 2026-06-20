package com.test.backend.telemetry.interfaces.rest;

import com.test.backend.telemetry.domain.model.commands.DeleteMetricTypeCommand;
import com.test.backend.telemetry.domain.model.queries.GetAllMetricTypesQuery;
import com.test.backend.telemetry.domain.model.queries.GetActiveMetricTypesQuery;
import com.test.backend.telemetry.domain.services.MetricTypeCommandService;
import com.test.backend.telemetry.domain.services.MetricTypeQueryService;
import com.test.backend.telemetry.interfaces.rest.resources.CreateMetricTypeResource;
import com.test.backend.telemetry.interfaces.rest.resources.MetricTypeResource;
import com.test.backend.telemetry.interfaces.rest.resources.UpdateMetricTypeResource;
import com.test.backend.telemetry.interfaces.rest.transform.CreateMetricTypeCommandFromResourceAssembler;
import com.test.backend.telemetry.interfaces.rest.transform.MetricTypeResourceFromEntityAssembler;
import com.test.backend.telemetry.interfaces.rest.transform.UpdateMetricTypeCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Metric Types", description = "Metric Type catalog management API")
@RestController
@RequestMapping("/api/v1/metric-types")
public class MetricTypeController {

    private final MetricTypeCommandService metricTypeCommandService;
    private final MetricTypeQueryService metricTypeQueryService;

    public MetricTypeController(MetricTypeCommandService metricTypeCommandService,
                                MetricTypeQueryService metricTypeQueryService) {
        this.metricTypeCommandService = metricTypeCommandService;
        this.metricTypeQueryService = metricTypeQueryService;
    }

    @GetMapping
    @Operation(summary = "Get all metric types")
    public ResponseEntity<List<MetricTypeResource>> getAllMetricTypes() {
        var metricTypes = metricTypeQueryService.handle(new GetAllMetricTypesQuery());
        var resources = metricTypes.stream()
                .map(MetricTypeResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active metric types only")
    public ResponseEntity<List<MetricTypeResource>> getActiveMetricTypes() {
        var metricTypes = metricTypeQueryService.handle(new GetActiveMetricTypesQuery());
        var resources = metricTypes.stream()
                .map(MetricTypeResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PostMapping
    @Operation(summary = "Create a new metric type")
    public ResponseEntity<MetricTypeResource> createMetricType(@RequestBody CreateMetricTypeResource resource) {
        var command = CreateMetricTypeCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = metricTypeCommandService.handle(command);
        return result.map(mt -> new ResponseEntity<>(MetricTypeResourceFromEntityAssembler.toResourceFromEntity(mt), HttpStatus.CREATED))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing metric type")
    public ResponseEntity<MetricTypeResource> updateMetricType(@PathVariable Long id, @RequestBody UpdateMetricTypeResource resource) {
        var command = UpdateMetricTypeCommandFromResourceAssembler.toCommandFromResource(id, resource);
        var result = metricTypeCommandService.handle(command);
        return result.map(mt -> ResponseEntity.ok(MetricTypeResourceFromEntityAssembler.toResourceFromEntity(mt)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate a metric type (soft delete)")
    public ResponseEntity<?> deleteMetricType(@PathVariable Long id) {
        try {
            metricTypeCommandService.handle(new DeleteMetricTypeCommand(id));
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
