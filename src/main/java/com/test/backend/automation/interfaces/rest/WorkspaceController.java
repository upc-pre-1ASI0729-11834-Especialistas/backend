package com.test.backend.automation.interfaces.rest;

import com.test.backend.automation.domain.model.aggregates.UserProfile;
import com.test.backend.automation.domain.model.entities.UserWorkspaceAccess;
import com.test.backend.automation.infrastructure.persistence.jpa.repositories.UserProfileRepository;
import com.test.backend.automation.infrastructure.persistence.jpa.repositories.UserWorkspaceAccessRepository;
import com.test.backend.labs.domain.model.aggregates.Workspace;
import com.test.backend.labs.infrastructure.persistence.jpa.repositories.WorkspaceRepository;
import com.test.backend.shared.application.CurrentWorkspaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Workspaces", description = "Workspaces management API")
@RestController
@RequestMapping("/api/v1/workspaces")
public class WorkspaceController {

    private final WorkspaceRepository workspaceRepository;
    private final UserWorkspaceAccessRepository userWorkspaceAccessRepository;
    private final UserProfileRepository userProfileRepository;
    private final CurrentWorkspaceService currentWorkspaceService;

    public WorkspaceController(WorkspaceRepository workspaceRepository,
                               UserWorkspaceAccessRepository userWorkspaceAccessRepository,
                               UserProfileRepository userProfileRepository,
                               CurrentWorkspaceService currentWorkspaceService) {
        this.workspaceRepository = workspaceRepository;
        this.userWorkspaceAccessRepository = userWorkspaceAccessRepository;
        this.userProfileRepository = userProfileRepository;
        this.currentWorkspaceService = currentWorkspaceService;
    }

    public record WorkspaceResource(Long id, String name, String code, String roleName) {}

    @GetMapping("/my-workspaces")
    @Operation(summary = "Get all workspaces the current user has access to")
    public ResponseEntity<List<WorkspaceResource>> getMyWorkspaces() {
        var profileOpt = currentWorkspaceService.getCurrentUserProfile();
        if (profileOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        var profile = profileOpt.get();
        var accesses = userWorkspaceAccessRepository.findByUserProfileId(profile.getId());
        var resources = accesses.stream()
                .filter(a -> a.getWorkspace() != null)
                .map(a -> new WorkspaceResource(
                        a.getWorkspace().getId(),
                        a.getWorkspace().getName(),
                        a.getWorkspace().getCode(),
                        a.getRole() != null ? a.getRole().getName() : "User"
                ))
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PostMapping("/{workspaceId}/switch")
    @Operation(summary = "Switch the active workspace context of the user")
    public ResponseEntity<Void> switchWorkspace(@PathVariable Long workspaceId) {
        var profileOpt = currentWorkspaceService.getCurrentUserProfile();
        if (profileOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        var profile = profileOpt.get();
        var accessOpt = userWorkspaceAccessRepository.findByUserProfileIdAndWorkspaceId(profile.getId(), workspaceId);
        if (accessOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        var access = accessOpt.get();
        
        // Update user profile active workspace context and role context
        profile.setWorkspaceId(workspaceId);
        if (access.getRole() != null) {
            profile.setRole(access.getRole());
        }
        userProfileRepository.save(profile);
        
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{workspaceId}")
    @Operation(summary = "Update the workspace name")
    public ResponseEntity<WorkspaceResource> updateWorkspace(@PathVariable Long workspaceId, @RequestBody WorkspaceResource resource) {
        var profileOpt = currentWorkspaceService.getCurrentUserProfile();
        if (profileOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        var profile = profileOpt.get();
        var accessOpt = userWorkspaceAccessRepository.findByUserProfileIdAndWorkspaceId(profile.getId(), workspaceId);
        if (accessOpt.isEmpty() || accessOpt.get().getRole() == null || !"Administrator".equalsIgnoreCase(accessOpt.get().getRole().getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        var workspaceOpt = workspaceRepository.findById(workspaceId);
        if (workspaceOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        var workspace = workspaceOpt.get();
        workspace.setName(resource.name());
        workspaceRepository.save(workspace);
        
        return ResponseEntity.ok(new WorkspaceResource(
                workspace.getId(),
                workspace.getName(),
                workspace.getCode(),
                accessOpt.get().getRole().getName()
        ));
    }
}
