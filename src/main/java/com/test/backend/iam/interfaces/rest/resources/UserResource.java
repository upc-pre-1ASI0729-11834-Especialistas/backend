package com.test.backend.iam.interfaces.rest.resources;

import java.util.List;

public record UserResource(Long id, String email, String fullName, List<String> roles) {
}
