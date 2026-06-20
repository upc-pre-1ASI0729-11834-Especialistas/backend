package com.test.backend.iam.interfaces.rest.resources;

public record AuthenticatedUserResource(Long id, String email, String fullName, String token) {
}
