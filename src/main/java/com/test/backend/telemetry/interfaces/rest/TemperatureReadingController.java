package com.test.backend.telemetry.interfaces.rest;

import com.test.backend.labs.domain.model.aggregates.Laboratory;
import com.test.backend.labs.infrastructure.persistence.jpa.repositories.LaboratoryRepository;
import com.test.backend.telemetry.domain.model.aggregates.SensorReading;
import com.test.backend.telemetry.domain.model.queries.GetAllSensorReadingsQuery;
import com.test.backend.telemetry.domain.services.SensorReadingQueryService;
import com.test.backend.telemetry.interfaces.rest.resources.TemperatureReadingResource;
import com.test.backend.telemetry.infrastructure.persistence.jpa.repositories.SensorReadingRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "Telemetry Readings", description = "Generic Telemetry Readings API — works for any metric type")
@RestController
@RequestMapping("/api/v1/telemetry")
public class TemperatureReadingController {

    private final SensorReadingQueryService sensorReadingQueryService;
    private final SensorReadingRepository sensorReadingRepository;
    private final LaboratoryRepository laboratoryRepository;

    public TemperatureReadingController(SensorReadingQueryService sensorReadingQueryService,
                                         SensorReadingRepository sensorReadingRepository,
                                         LaboratoryRepository laboratoryRepository) {
        this.sensorReadingQueryService = sensorReadingQueryService;
        this.sensorReadingRepository = sensorReadingRepository;
        this.laboratoryRepository = laboratoryRepository;
    }

    /**
     * Generic endpoint: returns chart-ready readings for any metric type.
     * Usage: GET /api/v1/telemetry/readings?metricKey=temperature
     *        GET /api/v1/telemetry/readings?metricKey=co2
     *        GET /api/v1/telemetry/readings?metricKey=humidity
     */
    @GetMapping("/readings")
    @Operation(summary = "Get historical readings for a specific metric type")
    public ResponseEntity<List<TemperatureReadingResource>> getReadingsByMetricKey(
            @RequestParam String metricKey) {

        List<SensorReading> readings = sensorReadingRepository.findByMetricTypeKey(metricKey);

        Map<String, List<SensorReading>> grouped = readings.stream()
                .filter(r -> r.getDate() != null && r.getLaboratory() != null)
                .collect(Collectors.groupingBy(SensorReading::getDate));

        List<TemperatureReadingResource> resources = new ArrayList<>();
        long idCounter = 1L;

        List<String> sortedDates = new ArrayList<>(grouped.keySet());
        Collections.sort(sortedDates);

        for (String date : sortedDates) {
            List<SensorReading> dateReadings = grouped.get(date);
            Map<String, Double> values = new HashMap<>();
            for (var r : dateReadings) {
                if (r.getLaboratory() != null) {
                    values.put(r.getLaboratory().getId().toString(), r.getValue());
                }
            }
            resources.add(new TemperatureReadingResource(idCounter++, date, values));
        }

        return ResponseEntity.ok(resources);
    }

    /**
     * Legacy endpoint preserved for backward compatibility.
     * Equivalent to: GET /api/v1/telemetry/readings?metricKey=temperature
     */
    @GetMapping("/temperature-readings")
    @Operation(summary = "Get historical temperature readings (legacy endpoint)")
    public ResponseEntity<List<TemperatureReadingResource>> getAllTemperatureReadings() {
        return getReadingsByMetricKey("temperature");
    }
}
