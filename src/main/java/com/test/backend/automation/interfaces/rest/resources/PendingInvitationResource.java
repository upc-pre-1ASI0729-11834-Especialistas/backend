package com.test.backend.automation.interfaces.rest.resources;

public record PendingInvitationResource(
    Long id,
    String email,
    String role,
    String sentTimeAgo
) {}
