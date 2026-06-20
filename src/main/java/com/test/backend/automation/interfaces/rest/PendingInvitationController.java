package com.test.backend.automation.interfaces.rest;

import com.test.backend.automation.domain.model.aggregates.PendingInvitation;
import com.test.backend.automation.domain.model.aggregates.UserProfile;
import com.test.backend.automation.domain.model.commands.CreatePendingInvitationCommand;
import com.test.backend.automation.domain.model.entities.LabUserAccess;
import com.test.backend.automation.domain.model.entities.Role;
import com.test.backend.automation.domain.model.entities.UserWorkspaceAccess;
import com.test.backend.automation.infrastructure.persistence.jpa.repositories.PendingInvitationRepository;
import com.test.backend.automation.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.test.backend.automation.infrastructure.persistence.jpa.repositories.UserProfileRepository;
import com.test.backend.automation.infrastructure.persistence.jpa.repositories.UserWorkspaceAccessRepository;
import com.test.backend.automation.interfaces.rest.resources.PendingInvitationResource;
import com.test.backend.labs.domain.model.aggregates.Workspace;
import com.test.backend.labs.infrastructure.persistence.jpa.repositories.LaboratoryRepository;
import com.test.backend.labs.infrastructure.persistence.jpa.repositories.WorkspaceRepository;
import com.test.backend.shared.application.CurrentWorkspaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Pending Invitations", description = "Pending Invitations management API")
@RestController
@RequestMapping("/api/v1/pending-invitations")
public class PendingInvitationController {

    private final PendingInvitationRepository pendingInvitationRepository;
    private final RoleRepository roleRepository;
    private final CurrentWorkspaceService currentWorkspaceService;
    private final WorkspaceRepository workspaceRepository;
    private final UserWorkspaceAccessRepository userWorkspaceAccessRepository;
    private final UserProfileRepository userProfileRepository;
    private final LaboratoryRepository laboratoryRepository;

    public PendingInvitationController(PendingInvitationRepository pendingInvitationRepository,
                                       RoleRepository roleRepository,
                                       CurrentWorkspaceService currentWorkspaceService,
                                       WorkspaceRepository workspaceRepository,
                                       UserWorkspaceAccessRepository userWorkspaceAccessRepository,
                                       UserProfileRepository userProfileRepository,
                                       LaboratoryRepository laboratoryRepository) {
        this.pendingInvitationRepository = pendingInvitationRepository;
        this.roleRepository = roleRepository;
        this.currentWorkspaceService = currentWorkspaceService;
        this.workspaceRepository = workspaceRepository;
        this.userWorkspaceAccessRepository = userWorkspaceAccessRepository;
        this.userProfileRepository = userProfileRepository;
        this.laboratoryRepository = laboratoryRepository;
    }

    @GetMapping
    @Operation(summary = "Get all pending invitations created by active workspace")
    public ResponseEntity<List<PendingInvitationResource>> getAllInvitations() {
        var workspaceIdOpt = currentWorkspaceService.getCurrentWorkspaceId();
        if (workspaceIdOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        var invitations = pendingInvitationRepository.findByWorkspaceId(workspaceIdOpt.get());
        var resources = invitations.stream()
                .map(this::toResource)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/my-invitations")
    @Operation(summary = "Get all pending invitations sent to currently authenticated user")
    public ResponseEntity<List<PendingInvitationResource>> getMyInvitations() {
        var emailOpt = currentWorkspaceService.getCurrentUserEmail();
        if (emailOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        var invitations = pendingInvitationRepository.findAllByEmail(emailOpt.get());
        var resources = invitations.stream()
                .map(this::toResource)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PostMapping
    @Operation(summary = "Create a new pending invitation")
    public ResponseEntity<PendingInvitationResource> createInvitation(@RequestBody CreatePendingInvitationCommand command) {
        if (command.email() == null || command.email().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        var workspaceIdOpt = currentWorkspaceService.getCurrentWorkspaceId();
        if (workspaceIdOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Long workspaceId = workspaceIdOpt.get();

        // Find or create role
        String roleName = command.role() != null ? command.role() : "User";
        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(roleName);
                    newRole.setDescription(roleName + " role");
                    return roleRepository.save(newRole);
                });

        var invitation = new PendingInvitation(command, role, workspaceId);
        pendingInvitationRepository.save(invitation);

        return new ResponseEntity<>(toResource(invitation), HttpStatus.CREATED);
    }

    @PostMapping("/{invitationId}/accept")
    @Operation(summary = "Accept a pending invitation")
    public ResponseEntity<Void> acceptInvitation(@PathVariable Long invitationId) {
        var emailOpt = currentWorkspaceService.getCurrentUserEmail();
        if (emailOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = emailOpt.get();

        var invitationOpt = pendingInvitationRepository.findById(invitationId);
        if (invitationOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var invitation = invitationOpt.get();
        if (!email.equalsIgnoreCase(invitation.getEmail())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var profileOpt = userProfileRepository.findByEmail(email);
        if (profileOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        var profile = profileOpt.get();

        var workspaceOpt = workspaceRepository.findById(invitation.getWorkspaceId());
        if (workspaceOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        var workspace = workspaceOpt.get();

        // Create workspace membership if not exists
        var accessOpt = userWorkspaceAccessRepository.findByUserProfileIdAndWorkspaceId(profile.getId(), workspace.getId());
        if (accessOpt.isEmpty()) {
            var workspaceAccess = new UserWorkspaceAccess();
            workspaceAccess.setUserProfile(profile);
            workspaceAccess.setWorkspace(workspace);
            workspaceAccess.setRole(invitation.getRole());
            userWorkspaceAccessRepository.save(workspaceAccess);
        }

        // Add laboratory access permissions
        if (invitation.getLaboratoryIds() != null && !invitation.getLaboratoryIds().isEmpty()) {
            for (Long labId : invitation.getLaboratoryIds()) {
                laboratoryRepository.findById(labId).ifPresent(lab -> {
                    // Check if already has access
                    boolean hasAccess = profile.getLabAccesses().stream()
                            .anyMatch(a -> a.getLaboratory().getId().equals(lab.getId()));
                    if (!hasAccess) {
                        var labAccess = new LabUserAccess();
                        labAccess.setUserProfile(profile);
                        labAccess.setLaboratory(lab);
                        profile.getLabAccesses().add(labAccess);
                    }
                });
            }
            userProfileRepository.save(profile);
        }

        pendingInvitationRepository.delete(invitation);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{invitationId}/reject")
    @Operation(summary = "Reject a pending invitation")
    public ResponseEntity<Void> rejectInvitation(@PathVariable Long invitationId) {
        var emailOpt = currentWorkspaceService.getCurrentUserEmail();
        if (emailOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = emailOpt.get();

        var invitationOpt = pendingInvitationRepository.findById(invitationId);
        if (invitationOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var invitation = invitationOpt.get();
        if (!email.equalsIgnoreCase(invitation.getEmail())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        pendingInvitationRepository.delete(invitation);
        return ResponseEntity.ok().build();
    }

    private PendingInvitationResource toResource(PendingInvitation invitation) {
        String roleName = invitation.getRole() != null ? invitation.getRole().getName() : "";
        
        String timeAgo = "Unknown";
        if (invitation.getSentAt() != null) {
            long diffMins = Duration.between(invitation.getSentAt(), LocalDateTime.now()).toMinutes();
            if (diffMins < 1) {
                timeAgo = "Just now";
            } else if (diffMins < 60) {
                timeAgo = diffMins + " mins ago";
            } else {
                long diffHours = diffMins / 60;
                if (diffHours < 24) {
                    timeAgo = diffHours + " hours ago";
                } else {
                    long diffDays = diffHours / 24;
                    timeAgo = diffDays + " days ago";
                }
            }
        }

        String workspaceName = "Unknown Workspace";
        if (invitation.getWorkspaceId() != null) {
            var workspaceOpt = workspaceRepository.findById(invitation.getWorkspaceId());
            if (workspaceOpt.isPresent()) {
                workspaceName = workspaceOpt.get().getName();
            }
        }

        return new PendingInvitationResource(
                invitation.getId(),
                invitation.getEmail(),
                roleName,
                timeAgo,
                invitation.getLaboratoryIds(),
                invitation.getWorkspaceId(),
                workspaceName
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel a pending invitation")
    public ResponseEntity<Void> cancelInvitation(@PathVariable Long id) {
        var workspaceIdOpt = currentWorkspaceService.getCurrentWorkspaceId();
        if (workspaceIdOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        var invitationOpt = pendingInvitationRepository.findByIdAndWorkspaceId(id, workspaceIdOpt.get());
        if (invitationOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        pendingInvitationRepository.delete(invitationOpt.get());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/resend")
    @Operation(summary = "Resend a pending invitation")
    public ResponseEntity<PendingInvitationResource> resendInvitation(@PathVariable Long id) {
        var workspaceIdOpt = currentWorkspaceService.getCurrentWorkspaceId();
        if (workspaceIdOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        var invitationOpt = pendingInvitationRepository.findByIdAndWorkspaceId(id, workspaceIdOpt.get());
        if (invitationOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var invitation = invitationOpt.get();
        invitation.setSentAt(LocalDateTime.now());
        pendingInvitationRepository.save(invitation);
        return ResponseEntity.ok(toResource(invitation));
    }
}
