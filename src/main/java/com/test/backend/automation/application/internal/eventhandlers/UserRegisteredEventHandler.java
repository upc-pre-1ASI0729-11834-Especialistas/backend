package com.test.backend.automation.application.internal.eventhandlers;

import com.test.backend.automation.domain.model.aggregates.UserProfile;
import com.test.backend.automation.domain.model.entities.Role;
import com.test.backend.automation.domain.model.entities.UserWorkspaceAccess;
import com.test.backend.automation.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.test.backend.automation.infrastructure.persistence.jpa.repositories.UserProfileRepository;
import com.test.backend.automation.infrastructure.persistence.jpa.repositories.UserWorkspaceAccessRepository;
import com.test.backend.iam.domain.model.events.UserRegisteredEvent;
import com.test.backend.labs.domain.model.aggregates.Workspace;
import com.test.backend.labs.infrastructure.persistence.jpa.repositories.WorkspaceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class UserRegisteredEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(UserRegisteredEventHandler.class);

    private final UserProfileRepository userProfileRepository;
    private final RoleRepository roleRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserWorkspaceAccessRepository userWorkspaceAccessRepository;

    public UserRegisteredEventHandler(UserProfileRepository userProfileRepository,
                                      RoleRepository roleRepository,
                                      WorkspaceRepository workspaceRepository,
                                      UserWorkspaceAccessRepository userWorkspaceAccessRepository) {
        this.userProfileRepository = userProfileRepository;
        this.roleRepository = roleRepository;
        this.workspaceRepository = workspaceRepository;
        this.userWorkspaceAccessRepository = userWorkspaceAccessRepository;
    }

    @EventListener
    @Transactional
    public void handle(UserRegisteredEvent event) {
        logger.info("Handling UserRegisteredEvent for email: {}", event.email());

        if (userProfileRepository.findByEmail(event.email()).isPresent()) {
            logger.info("UserProfile already exists for email: {}. Skipping.", event.email());
            return;
        }

        // Direct registration: create a brand new workspace
        var newWorkspace = new Workspace("Workspace of " + event.fullName());
        newWorkspace = workspaceRepository.save(newWorkspace);
        Long workspaceId = newWorkspace.getId();
        logger.info("Created new Workspace with ID: {} for user: {}", workspaceId, event.email());

        String roleName = "Administrator";
        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    var newRole = new Role();
                    newRole.setName(roleName);
                    newRole.setDescription(roleName + " role");
                    return roleRepository.save(newRole);
                });

        // Determine access tier based on role
        String accessTier = "Tier 1"; // Administrator is always Tier 1
        String employeeId = "EMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        UserProfile profile = new UserProfile();
        profile.setEmail(event.email());
        profile.setFullName(event.fullName());
        profile.setRole(role);
        profile.setProfessionalTitle(roleName);
        profile.setEmployeeId(employeeId);
        profile.setSystemState("Active");
        profile.setAccessTier(accessTier);
        profile.setDefaultStartShift("08:00 AM");
        profile.setShiftDuration("8 Hours");
        profile.setAutoGenerateShiftReport(false);
        profile.setWorkspaceId(workspaceId);

        profile = userProfileRepository.save(profile);
        logger.info("UserProfile created successfully for user: {}", event.email());

        // Create the workspace access record linking user to their own workspace as Administrator
        UserWorkspaceAccess workspaceAccess = new UserWorkspaceAccess();
        workspaceAccess.setUserProfile(profile);
        workspaceAccess.setWorkspace(newWorkspace);
        workspaceAccess.setRole(role);
        userWorkspaceAccessRepository.save(workspaceAccess);
        logger.info("Workspace membership registered for user {} in workspace {}", event.email(), workspaceId);
    }
}
