package com.test.backend.iam.interfaces.rest.transform;

import com.test.backend.iam.domain.model.valueobjects.AuthenticatedUser;
import com.test.backend.iam.interfaces.rest.resources.AuthenticatedUserResource;

public class AuthenticatedUserResourceFromEntityAssembler {
    public static AuthenticatedUserResource toResourceFromEntity(AuthenticatedUser entity) {
        return new AuthenticatedUserResource(
                entity.user().getId(),
                entity.user().getEmail(),
                entity.user().getFullName(),
                entity.token()
        );
    }
}
