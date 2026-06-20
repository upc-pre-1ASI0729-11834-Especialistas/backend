package com.test.backend.automation.interfaces.rest.resources;

import java.util.List;

public record PendingInvitationResource(
    Long id,
    String email,
    String role,
    String sentTimeAgo,
    List<Long> laboratoryIds,
    Long workspaceId,
    String workspaceName
) {}
