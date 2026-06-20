package com.test.backend.iam.domain.model.events;

/**
 * Event published when a new user registers in the IAM context.
 * Captured by other contexts (like Automation) to create user profiles.
 */
public record UserRegisteredEvent(String email, String fullName) {
}
