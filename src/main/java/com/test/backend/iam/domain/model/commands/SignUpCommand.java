package com.test.backend.iam.domain.model.commands;

import com.test.backend.iam.domain.model.valueobjects.Roles;
import java.util.List;

public record SignUpCommand(String email, String password, String fullName, List<Roles> roles) {
}
