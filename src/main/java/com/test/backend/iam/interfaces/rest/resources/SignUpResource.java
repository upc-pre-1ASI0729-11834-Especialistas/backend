package com.test.backend.iam.interfaces.rest.resources;

import java.util.List;

public record SignUpResource(String email, String password, String fullName, List<String> roles) {
}
