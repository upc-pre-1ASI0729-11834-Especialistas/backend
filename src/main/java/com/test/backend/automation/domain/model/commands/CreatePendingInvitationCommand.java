package com.test.backend.automation.domain.model.commands;

import java.util.List;

public record CreatePendingInvitationCommand(
    String email,
    String role,
    List<Long> laboratoryIds
) {}
