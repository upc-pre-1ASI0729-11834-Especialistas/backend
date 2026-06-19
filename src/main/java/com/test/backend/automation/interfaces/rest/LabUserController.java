package com.test.backend.automation.interfaces.rest;

import com.test.backend.automation.domain.model.aggregates.UserProfile;
import com.test.backend.automation.infrastructure.persistence.jpa.repositories.UserProfileRepository;
import com.test.backend.automation.infrastructure.persistence.jpa.repositories.UserWorkspaceAccessRepository;
import com.test.backend.automation.interfaces.rest.resources.LabUserResource;
import com.test.backend.shared.application.CurrentWorkspaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Lab Users", description = "Lab Users management API (Denormalized View)")
@RestController
@RequestMapping("/api/v1/lab-users")
public class LabUserController {

    private final UserProfileRepository userProfileRepository;
    private final UserWorkspaceAccessRepository userWorkspaceAccessRepository;
    private final CurrentWorkspaceService currentWorkspaceService;

    public LabUserController(UserProfileRepository userProfileRepository,
                              UserWorkspaceAccessRepository userWorkspaceAccessRepository,
                              CurrentWorkspaceService currentWorkspaceService) {
        this.userProfileRepository = userProfileRepository;
        this.userWorkspaceAccessRepository = userWorkspaceAccessRepository;
        this.currentWorkspaceService = currentWorkspaceService;
    }

    @GetMapping
    @Operation(summary = "Get all lab users (denormalized view)")
    public ResponseEntity<List<LabUserResource>> getAllLabUsers() {
        var workspaceIdOpt = currentWorkspaceService.getCurrentWorkspaceId();
        if (workspaceIdOpt.isEmpty()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }
        Long workspaceId = workspaceIdOpt.get();
        var accesses = userWorkspaceAccessRepository.findByWorkspaceId(workspaceId);
        var resources = accesses.stream()
                .filter(a -> a.getUserProfile() != null)
                .map(a -> toResource(a.getUserProfile(), a.getRole() != null ? a.getRole().getName() : "User", workspaceId))
                .toList();
        return ResponseEntity.ok(resources);
    }

    private LabUserResource toResource(UserProfile profile, String roleName, Long workspaceId) {
        String labsAccess = "None";
        if (profile.getLabAccesses() != null && !profile.getLabAccesses().isEmpty()) {
            labsAccess = profile.getLabAccesses().stream()
                    .filter(a -> a.getLaboratory() != null && a.getLaboratory().getWorkspace() != null && a.getLaboratory().getWorkspace().getId().equals(workspaceId))
                    .map(a -> a.getLaboratory().getName())
                    .collect(Collectors.joining(", "));
        }
        if (labsAccess.isEmpty()) labsAccess = "None";

        String initials = "";
        if (profile.getFullName() != null && !profile.getFullName().isBlank()) {
            String[] parts = profile.getFullName().split("\\s+");
            if (parts.length > 0 && !parts[0].isBlank()) {
                initials += parts[0].substring(0, 1).toUpperCase();
            }
            if (parts.length > 1 && !parts[1].isBlank()) {
                initials += parts[1].substring(0, 1).toUpperCase();
            }
        }
        if (initials.isEmpty()) initials = "U";

        String[] colors = {"#EF4444", "#F59E0B", "#10B981", "#3B82F6", "#6366F1", "#8B5CF6", "#EC4899"};
        int colorIdx = Math.abs(profile.getFullName() != null ? profile.getFullName().hashCode() : 0) % colors.length;
        String color = colors[colorIdx];

        String status = profile.getSystemState() != null ? profile.getSystemState() : "Active";

        return new LabUserResource(
                profile.getId(),
                profile.getFullName(),
                profile.getEmail(),
                roleName,
                labsAccess,
                profile.getLastLogin(),
                status,
                initials,
                color
        );
    }
}
