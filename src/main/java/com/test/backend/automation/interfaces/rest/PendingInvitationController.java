package com.test.backend.automation.interfaces.rest;

import com.test.backend.automation.domain.model.aggregates.PendingInvitation;
import com.test.backend.automation.domain.model.commands.CreatePendingInvitationCommand;
import com.test.backend.automation.domain.model.entities.Role;
import com.test.backend.automation.infrastructure.persistence.jpa.repositories.PendingInvitationRepository;
import com.test.backend.automation.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.test.backend.automation.interfaces.rest.resources.PendingInvitationResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Tag(name = "Pending Invitations", description = "Pending Invitations management API")
@RestController
@RequestMapping("/api/v1/pending-invitations")
public class PendingInvitationController {

    private final PendingInvitationRepository pendingInvitationRepository;
    private final RoleRepository roleRepository;

    public PendingInvitationController(PendingInvitationRepository pendingInvitationRepository,
                                       RoleRepository roleRepository) {
        this.pendingInvitationRepository = pendingInvitationRepository;
        this.roleRepository = roleRepository;
    }

    @GetMapping
    @Operation(summary = "Get all pending invitations")
    public ResponseEntity<List<PendingInvitationResource>> getAllInvitations() {
        var invitations = pendingInvitationRepository.findAll();
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

        // Find or create role
        String roleName = command.role() != null ? command.role() : "User";
        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(roleName);
                    newRole.setDescription(roleName + " role");
                    return roleRepository.save(newRole);
                });

        var invitation = new PendingInvitation(command, role);
        pendingInvitationRepository.save(invitation);

        return new ResponseEntity<>(toResource(invitation), HttpStatus.CREATED);
    }

    private PendingInvitationResource toResource(PendingInvitation invitation) {
        String roleName = invitation.getRole() != null ? invitation.getRole().getName() : "";
        
        String timeAgo = "Unknown";
        if (invitation.getSentAt() != null) {
            long diffMs = new Date().getTime() - invitation.getSentAt().getTime();
            long diffMins = diffMs / (60 * 1000);
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

        return new PendingInvitationResource(
                invitation.getId(),
                invitation.getEmail(),
                roleName,
                timeAgo
        );
    }
}
