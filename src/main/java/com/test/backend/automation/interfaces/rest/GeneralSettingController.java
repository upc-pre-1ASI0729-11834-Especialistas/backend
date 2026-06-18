package com.test.backend.automation.interfaces.rest;

import com.test.backend.automation.infrastructure.persistence.jpa.repositories.GeneralSettingRepository;
import com.test.backend.automation.interfaces.rest.resources.GeneralSettingResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "General Settings", description = "General Settings management API")
@RestController
@RequestMapping("/api/v1/general-settings")
public class GeneralSettingController {

    private final GeneralSettingRepository generalSettingRepository;

    public GeneralSettingController(GeneralSettingRepository generalSettingRepository) {
        this.generalSettingRepository = generalSettingRepository;
    }

    @GetMapping
    @Operation(summary = "Get all general settings")
    public ResponseEntity<List<GeneralSettingResource>> getAllSettings() {
        var settings = generalSettingRepository.findAll();
        var resources = settings.stream()
                .map(s -> new GeneralSettingResource(
                        s.getId(),
                        s.getSettingKey(),
                        s.getValue(),
                        s.getCategory(),
                        s.getDescription()
                ))
                .toList();
        return ResponseEntity.ok(resources);
    }
}
