package com.test.backend.telemetry.interfaces.rest;

import com.test.backend.labs.domain.model.aggregates.Laboratory;
import com.test.backend.labs.infrastructure.persistence.jpa.repositories.LaboratoryRepository;
import com.test.backend.telemetry.domain.model.aggregates.SensorReading;
import com.test.backend.telemetry.domain.model.queries.GetAllSensorReadingsQuery;
import com.test.backend.telemetry.domain.services.SensorReadingQueryService;
import com.test.backend.telemetry.interfaces.rest.resources.TemperatureReadingResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "Telemetry Temperature Readings", description = "Telemetry Temperature Readings API")
@RestController
@RequestMapping("/api/v1/telemetry/temperature-readings")
public class TemperatureReadingController {

    private final SensorReadingQueryService sensorReadingQueryService;
    private final LaboratoryRepository laboratoryRepository;

    public TemperatureReadingController(SensorReadingQueryService sensorReadingQueryService,
                                         LaboratoryRepository laboratoryRepository) {
        this.sensorReadingQueryService = sensorReadingQueryService;
        this.laboratoryRepository = laboratoryRepository;
    }

    @GetMapping
    @Operation(summary = "Get historical temperature readings")
    public ResponseEntity<List<TemperatureReadingResource>> getAllReadings() {
        var readings = sensorReadingQueryService.handle(new GetAllSensorReadingsQuery());
        
        List<Laboratory> labs = laboratoryRepository.findAll();
        Long lab01Id = labs.size() > 0 ? labs.get(0).getId() : -1L;
        Long lab02Id = labs.size() > 1 ? labs.get(1).getId() : -1L;

        Map<String, List<SensorReading>> grouped = readings.stream()
                .filter(r -> r.getDate() != null && r.getLaboratory() != null)
                .collect(Collectors.groupingBy(SensorReading::getDate));

        List<TemperatureReadingResource> resources = new ArrayList<>();
        long idCounter = 1L;

        // Sort dates to return in chronological order
        List<String> sortedDates = new ArrayList<>(grouped.keySet());
        Collections.sort(sortedDates);

        for (String date : sortedDates) {
            List<SensorReading> dateReadings = grouped.get(date);
            Double val1 = 20.0;
            Double val2 = 21.0;
            for (var r : dateReadings) {
                if (r.getLaboratory().getId().equals(lab01Id)) {
                    val1 = r.getValue();
                } else if (r.getLaboratory().getId().equals(lab02Id)) {
                    val2 = r.getValue();
                }
            }
            resources.add(new TemperatureReadingResource(idCounter++, date, val1, val2));
        }



        return ResponseEntity.ok(resources);
    }
}
