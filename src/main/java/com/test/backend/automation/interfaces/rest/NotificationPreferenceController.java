package com.test.backend.automation.interfaces.rest;

import com.test.backend.automation.infrastructure.persistence.jpa.repositories.NotificationPreferenceRepository;
import com.test.backend.automation.interfaces.rest.resources.NotificationPreferenceResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Notification Preferences", description = "Notification Preferences management API")
@RestController
@RequestMapping("/api/v1/notification-preferences")
public class NotificationPreferenceController {

    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final com.test.backend.automation.infrastructure.persistence.jpa.repositories.UserProfileRepository userProfileRepository;
    private final com.test.backend.shared.application.CurrentWorkspaceService currentWorkspaceService;

    public NotificationPreferenceController(NotificationPreferenceRepository notificationPreferenceRepository,
                                            com.test.backend.automation.infrastructure.persistence.jpa.repositories.UserProfileRepository userProfileRepository,
                                            com.test.backend.shared.application.CurrentWorkspaceService currentWorkspaceService) {
        this.notificationPreferenceRepository = notificationPreferenceRepository;
        this.userProfileRepository = userProfileRepository;
        this.currentWorkspaceService = currentWorkspaceService;
    }

    @GetMapping
    @Operation(summary = "Get all notification preferences")
    public ResponseEntity<List<NotificationPreferenceResource>> getAllPreferences() {
        var profileOpt = currentWorkspaceService.getCurrentUserProfile();
        if (profileOpt.isEmpty()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }
        var preferences = notificationPreferenceRepository.findByUserProfileId(profileOpt.get().getId());
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

    @PutMapping("/{id}")
    @Operation(summary = "Update notification preference")
    public ResponseEntity<NotificationPreferenceResource> updatePreference(@PathVariable Long id, @RequestBody NotificationPreferenceResource resource) {
        var profileOpt = currentWorkspaceService.getCurrentUserProfile();
        if (profileOpt.isEmpty()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }
        var prefOpt = notificationPreferenceRepository.findById(id);
        if (prefOpt.isEmpty() || !prefOpt.get().getUserProfile().getId().equals(profileOpt.get().getId())) {
            return ResponseEntity.notFound().build();
        }
        var preference = prefOpt.get();
        preference.setEnabled(resource.isEnabled());
        if (resource.threshold() != null) {
            preference.setThreshold(resource.threshold());
        }
        if (resource.description() != null) {
            preference.setDescription(resource.description());
        }
        notificationPreferenceRepository.save(preference);
        return ResponseEntity.ok(new NotificationPreferenceResource(
                preference.getId(),
                preference.getChannel(),
                preference.isEnabled(),
                preference.getThreshold(),
                preference.getDescription()
        ));
    }
}
