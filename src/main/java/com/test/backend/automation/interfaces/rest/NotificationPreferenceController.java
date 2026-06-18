package com.test.backend.automation.interfaces.rest;

import com.test.backend.automation.infrastructure.persistence.jpa.repositories.NotificationPreferenceRepository;
import com.test.backend.automation.interfaces.rest.resources.NotificationPreferenceResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Notification Preferences", description = "Notification Preferences management API")
@RestController
@RequestMapping("/api/v1/notification-preferences")
public class NotificationPreferenceController {

    private final NotificationPreferenceRepository notificationPreferenceRepository;

    public NotificationPreferenceController(NotificationPreferenceRepository notificationPreferenceRepository) {
        this.notificationPreferenceRepository = notificationPreferenceRepository;
    }

    @GetMapping
    @Operation(summary = "Get all notification preferences")
    public ResponseEntity<List<NotificationPreferenceResource>> getAllPreferences() {
        var preferences = notificationPreferenceRepository.findAll();
        var resources = preferences.stream()
                .map(p -> new NotificationPreferenceResource(
                        p.getId(),
                        p.getChannel(),
                        p.isEnabled(),
                        p.getThreshold(),
                        p.getDescription()
                ))
                .toList();
        return ResponseEntity.ok(resources);
    }
}
