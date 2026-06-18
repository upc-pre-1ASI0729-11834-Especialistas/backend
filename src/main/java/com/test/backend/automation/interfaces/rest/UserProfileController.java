package com.test.backend.automation.interfaces.rest;

import com.test.backend.automation.domain.model.aggregates.UserProfile;
import com.test.backend.automation.domain.model.commands.UpdateUserProfileCommand;
import com.test.backend.automation.domain.model.entities.Role;
import com.test.backend.automation.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.test.backend.automation.infrastructure.persistence.jpa.repositories.UserProfileRepository;
import com.test.backend.automation.interfaces.rest.resources.UserProfileResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    public UserProfileController(UserProfileRepository userProfileRepository, RoleRepository roleRepository,
                                 com.test.backend.iam.infrastructure.persistence.jpa.repositories.UserRepository iamUserRepository) {
        this.userProfileRepository = userProfileRepository;
        this.roleRepository = roleRepository;
        this.iamUserRepository = iamUserRepository;
    }

    @GetMapping
    @Operation(summary = "Get all user profiles")
    public ResponseEntity<List<UserProfileResource>> getAllProfiles() {
        var profiles = userProfileRepository.findAll();
        var resources = profiles.stream()
                .map(this::toResource)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing user profile")
    public ResponseEntity<UserProfileResource> updateProfile(@PathVariable Long id, 
                                                            @RequestBody UserProfileResource resource) {
        var result = userProfileRepository.findById(id);
        if (result.isEmpty()) return ResponseEntity.notFound().build();

        var profile = result.get();
        
        Role role = profile.getRole();
        if (resource.role() != null && !resource.role().isBlank()) {
            role = roleRepository.findByName(resource.role())
                    .orElse(role);
        }

        var command = new UpdateUserProfileCommand(
                id,
                resource.fullName() != null ? resource.fullName() : profile.getFullName(),
                role != null ? role.getName() : null,
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

        profile.updateFrom(command, role);
        userProfileRepository.save(profile);

        var iamUserOptional = iamUserRepository.findByEmail(oldEmail);
        if (iamUserOptional.isPresent()) {
            var iamUser = iamUserOptional.get();
            if (newEmail != null) iamUser.setEmail(newEmail);
            if (command.fullName() != null) iamUser.setFullName(command.fullName());
            iamUserRepository.save(iamUser);
        }

        return ResponseEntity.ok(toResource(profile));
    }

    private UserProfileResource toResource(UserProfile profile) {
        String roleName = profile.getRole() != null ? profile.getRole().getName() : "";
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
                profile.isAutoGenerateShiftReport()
        );
    }
}
