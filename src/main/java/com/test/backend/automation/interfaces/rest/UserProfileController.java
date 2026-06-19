package com.test.backend.automation.interfaces.rest;

import com.test.backend.automation.domain.model.aggregates.UserProfile;
import com.test.backend.automation.domain.model.commands.UpdateUserProfileCommand;
import com.test.backend.automation.domain.model.entities.LabUserAccess;
import com.test.backend.automation.domain.model.entities.Role;
import com.test.backend.automation.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.test.backend.automation.infrastructure.persistence.jpa.repositories.UserProfileRepository;
import com.test.backend.automation.infrastructure.persistence.jpa.repositories.UserWorkspaceAccessRepository;
import com.test.backend.automation.interfaces.rest.resources.UserProfileResource;
import com.test.backend.labs.infrastructure.persistence.jpa.repositories.LaboratoryRepository;
import com.test.backend.shared.application.CurrentWorkspaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "User Profiles", description = "User Profiles management API")
@RestController
@RequestMapping("/api/v1/user-profiles")
public class UserProfileController {

    private final UserProfileRepository userProfileRepository;
    private final RoleRepository roleRepository;
    private final com.test.backend.iam.infrastructure.persistence.jpa.repositories.UserRepository iamUserRepository;
    private final CurrentWorkspaceService currentWorkspaceService;
    private final UserWorkspaceAccessRepository userWorkspaceAccessRepository;
    private final LaboratoryRepository laboratoryRepository;

    public UserProfileController(UserProfileRepository userProfileRepository,
                                 RoleRepository roleRepository,
                                 com.test.backend.iam.infrastructure.persistence.jpa.repositories.UserRepository iamUserRepository,
                                 CurrentWorkspaceService currentWorkspaceService,
                                 UserWorkspaceAccessRepository userWorkspaceAccessRepository,
                                 LaboratoryRepository laboratoryRepository) {
        this.userProfileRepository = userProfileRepository;
        this.roleRepository = roleRepository;
        this.iamUserRepository = iamUserRepository;
        this.currentWorkspaceService = currentWorkspaceService;
        this.userWorkspaceAccessRepository = userWorkspaceAccessRepository;
        this.laboratoryRepository = laboratoryRepository;
    }

    @GetMapping
    @Operation(summary = "Get all user profiles in active workspace")
    public ResponseEntity<List<UserProfileResource>> getAllProfiles() {
        var workspaceIdOpt = currentWorkspaceService.getCurrentWorkspaceId();
        if (workspaceIdOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Long workspaceId = workspaceIdOpt.get();
        var accesses = userWorkspaceAccessRepository.findByWorkspaceId(workspaceId);
        var resources = accesses.stream()
                .filter(a -> a.getUserProfile() != null)
                .map(a -> toResource(a.getUserProfile(), workspaceId, a.getRole() != null ? a.getRole().getName() : ""))
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing user profile")
    public ResponseEntity<UserProfileResource> updateProfile(@PathVariable Long id, 
                                                            @RequestBody UserProfileResource resource) {
        var workspaceIdOpt = currentWorkspaceService.getCurrentWorkspaceId();
        if (workspaceIdOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Long workspaceId = workspaceIdOpt.get();

        var result = userProfileRepository.findById(id);
        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var profile = result.get();

        // Check if target user belongs to active workspace
        var targetAccessOpt = userWorkspaceAccessRepository.findByUserProfileIdAndWorkspaceId(profile.getId(), workspaceId);
        if (targetAccessOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var targetAccess = targetAccessOpt.get();

        boolean isCallerAdmin = currentWorkspaceService.isCurrentUserAdmin();

        Role activeRole = targetAccess.getRole();
        if (isCallerAdmin && resource.role() != null && !resource.role().isBlank()) {
            activeRole = roleRepository.findByName(resource.role())
                    .orElseGet(() -> {
                        Role newRole = new Role();
                        newRole.setName(resource.role());
                        newRole.setDescription(resource.role() + " role");
                        return roleRepository.save(newRole);
                    });
            targetAccess.setRole(activeRole);
            userWorkspaceAccessRepository.save(targetAccess);
            
            // Sync with profile if target user currently has this workspace as active
            if (profile.getWorkspaceId().equals(workspaceId)) {
                profile.setRole(activeRole);
            }
        }

        // Update target user's laboratory access permissions in active workspace
        if (isCallerAdmin && resource.laboratoryIds() != null) {
            // Remove existing LabUserAccess records for user in laboratories belonging to active workspace
            profile.getLabAccesses().removeIf(a -> a.getLaboratory() != null 
                    && a.getLaboratory().getWorkspace() != null 
                    && a.getLaboratory().getWorkspace().getId().equals(workspaceId));

            // Create new LabUserAccess records for labs of active workspace
            for (Long labId : resource.laboratoryIds()) {
                laboratoryRepository.findById(labId).ifPresent(lab -> {
                    if (lab.getWorkspace() != null && lab.getWorkspace().getId().equals(workspaceId)) {
                        var labAccess = new LabUserAccess();
                        labAccess.setUserProfile(profile);
                        labAccess.setLaboratory(lab);
                        profile.getLabAccesses().add(labAccess);
                    }
                });
            }
        }

        var command = new UpdateUserProfileCommand(
                id,
                resource.fullName() != null ? resource.fullName() : profile.getFullName(),
                activeRole != null ? activeRole.getName() : null,
                resource.email() != null ? resource.email() : profile.getEmail(),
                resource.avatarUrl() != null ? resource.avatarUrl() : profile.getAvatarUrl(),
                resource.phoneNumber() != null ? resource.phoneNumber() : profile.getPhoneNumber(),
                resource.professionalTitle() != null ? resource.professionalTitle() : profile.getProfessionalTitle(),
                resource.employeeId() != null ? resource.employeeId() : profile.getEmployeeId(),
                resource.systemState() != null ? resource.systemState() : profile.getSystemState(),
                resource.accessTier() != null ? resource.accessTier() : profile.getAccessTier(),
                resource.defaultStartShift() != null ? resource.defaultStartShift() : profile.getDefaultStartShift(),
                resource.shiftDuration() != null ? resource.shiftDuration() : profile.getShiftDuration(),
                resource.autoGenerateShiftReport()
        );

        var oldEmail = profile.getEmail();
        var newEmail = command.email();

        if (newEmail != null && !newEmail.equals(oldEmail) && iamUserRepository.existsByEmail(newEmail)) {
            return ResponseEntity.badRequest().build();
        }

        profile.updateFrom(command, activeRole);
        userProfileRepository.save(profile);

        var iamUserOptional = iamUserRepository.findByEmail(oldEmail);
        if (iamUserOptional.isPresent()) {
            var iamUser = iamUserOptional.get();
            if (newEmail != null) iamUser.setEmail(newEmail);
            if (command.fullName() != null) iamUser.setFullName(command.fullName());
            iamUserRepository.save(iamUser);
        }

        return ResponseEntity.ok(toResource(profile, workspaceId, activeRole != null ? activeRole.getName() : ""));
    }

    private UserProfileResource toResource(UserProfile profile, Long workspaceId, String roleName) {
        List<Long> labIds = profile.getLabAccesses().stream()
                .filter(a -> a.getLaboratory() != null && a.getLaboratory().getWorkspace() != null && a.getLaboratory().getWorkspace().getId().equals(workspaceId))
                .map(a -> a.getLaboratory().getId())
                .toList();

        return new UserProfileResource(
                profile.getId(),
                profile.getFullName(),
                roleName,
                profile.getEmail(),
                profile.getAvatarUrl(),
                profile.getPhoneNumber(),
                profile.getProfessionalTitle(),
                profile.getEmployeeId(),
                profile.getSystemState(),
                profile.getAccessTier(),
                profile.getDefaultStartShift(),
                profile.getShiftDuration(),
                profile.isAutoGenerateShiftReport(),
                labIds
        );
    }
}
