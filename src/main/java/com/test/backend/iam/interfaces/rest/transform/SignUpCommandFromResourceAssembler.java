package com.test.backend.iam.interfaces.rest.transform;

import com.test.backend.iam.domain.model.commands.SignUpCommand;
import com.test.backend.iam.domain.model.valueobjects.Roles;
import com.test.backend.iam.interfaces.rest.resources.SignUpResource;

import java.util.ArrayList;

public class SignUpCommandFromResourceAssembler {
    public static SignUpCommand toCommandFromResource(SignUpResource resource) {
        var roles = new ArrayList<Roles>();
        if (resource.roles() != null) {
            resource.roles().forEach(role -> {
                try {
                    roles.add(Roles.valueOf(role));
                } catch (IllegalArgumentException e) {
                    // Ignore or log invalid roles
                }
            });
        }
        return new SignUpCommand(
                resource.email(),
                resource.password(),
                resource.fullName(),
                roles
        );
    }
}
