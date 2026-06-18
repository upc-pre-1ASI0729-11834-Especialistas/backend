package com.test.backend.automation.domain.model.commands;

public record CreatePendingInvitationCommand(
    String email,
    String role
) {}
