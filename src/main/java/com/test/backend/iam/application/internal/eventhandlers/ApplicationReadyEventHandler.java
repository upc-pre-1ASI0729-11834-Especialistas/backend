package com.test.backend.iam.application.internal.eventhandlers;

import com.test.backend.automation.domain.model.aggregates.UserProfile;
import com.test.backend.automation.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.test.backend.automation.infrastructure.persistence.jpa.repositories.UserProfileRepository;
import com.test.backend.iam.domain.model.commands.SeedRolesCommand;
import com.test.backend.iam.domain.model.commands.SignUpCommand;
import com.test.backend.iam.domain.model.queries.GetUserByEmailQuery;
import com.test.backend.iam.domain.model.valueobjects.Roles;
import com.test.backend.iam.domain.services.RoleCommandService;
import com.test.backend.iam.domain.services.UserCommandService;
import com.test.backend.iam.domain.services.UserQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApplicationReadyEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationReadyEventHandler.class);

    private final RoleCommandService roleCommandService;
    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;
    private final UserProfileRepository userProfileRepository;
    private final RoleRepository automationRoleRepository;

    public ApplicationReadyEventHandler(RoleCommandService roleCommandService,
                                         UserCommandService userCommandService,
                                         UserQueryService userQueryService,
                                         UserProfileRepository userProfileRepository,
                                         RoleRepository automationRoleRepository) {
        this.roleCommandService = roleCommandService;
        this.userCommandService = userCommandService;
        this.userQueryService = userQueryService;
        this.userProfileRepository = userProfileRepository;
        this.automationRoleRepository = automationRoleRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void on(ApplicationReadyEvent event) {
        logger.info("Application is ready. Starting IAM seeding process...");

        // 1. Seed IAM Roles
        roleCommandService.handle(new SeedRolesCommand());
        logger.info("IAM Roles seeded successfully.");

        // 2. Seed Default Admin
        String adminEmail = "admin@safelab.com";
        var adminUser = userQueryService.handle(new GetUserByEmailQuery(adminEmail));

        if (adminUser.isEmpty()) {
            var signUpCommand = new SignUpCommand(
                    adminEmail,
                    "admin123",
                    "SafeLab Administrator",
                    List.of(Roles.ROLE_ADMIN)
            );
            userCommandService.handle(signUpCommand);
            logger.info("Default admin user created: {} / admin123", adminEmail);
        } else {
            logger.info("Default admin user already exists. Skipping seeding.");
        }

        // 3. Seed Default Admin Profile in Automation Context
        var automationAdminRole = automationRoleRepository.findByName("Administrator")
                .orElseGet(() -> {
                    var role = new com.test.backend.automation.domain.model.entities.Role();
                    role.setName("Administrator");
                    role.setDescription("Administrator role");
                    return automationRoleRepository.save(role);
                });

        if (userProfileRepository.findByEmail(adminEmail).isEmpty()) {
            var profile = new UserProfile();
            profile.setEmail(adminEmail);
            profile.setFullName("SafeLab Administrator");
            profile.setRole(automationAdminRole);
            profile.setProfessionalTitle("Administrator");
            profile.setEmployeeId("EMP-00001");
            profile.setSystemState("Active");
            profile.setAccessTier("Tier 1");
            profile.setDefaultStartShift("08:00 AM");
            profile.setShiftDuration("8 Hours");
            profile.setAutoGenerateShiftReport(false);
            userProfileRepository.save(profile);
            logger.info("Default user profile created in automation context: {}", adminEmail);
        } else {
            logger.info("Default user profile already exists. Skipping seeding.");
        }
    }
}
