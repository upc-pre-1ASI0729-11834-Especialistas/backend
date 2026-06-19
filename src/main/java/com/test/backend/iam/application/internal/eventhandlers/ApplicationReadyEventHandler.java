package com.test.backend.iam.application.internal.eventhandlers;

import com.test.backend.iam.domain.model.commands.SeedRolesCommand;
import com.test.backend.iam.domain.services.RoleCommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ApplicationReadyEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationReadyEventHandler.class);

    private final RoleCommandService roleCommandService;

    public ApplicationReadyEventHandler(RoleCommandService roleCommandService) {
        this.roleCommandService = roleCommandService;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void on(ApplicationReadyEvent event) {
        logger.info("Application is ready. Starting IAM seeding process...");

        // 1. Seed IAM Roles
        roleCommandService.handle(new SeedRolesCommand());
        logger.info("IAM Roles seeded successfully.");
    }
}

