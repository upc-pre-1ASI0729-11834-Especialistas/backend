package com.test.backend.iam.domain.model.valueobjects;

import com.test.backend.iam.domain.model.aggregates.User;

public record AuthenticatedUser(User user, String token) {
}
