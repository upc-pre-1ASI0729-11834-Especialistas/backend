package com.test.backend.iam.interfaces.rest.transform;

import com.test.backend.iam.domain.model.aggregates.User;
import com.test.backend.iam.domain.model.entities.Role;
import com.test.backend.iam.interfaces.rest.resources.UserResource;

import java.util.stream.Collectors;

public class UserResourceFromEntityAssembler {
    public static UserResource toResourceFromEntity(User entity) {
        var roles = entity.getRoles().stream()
                .map(Role::getStringName)
                .collect(Collectors.toList());
        return new UserResource(
                entity.getId(),
                entity.getEmail(),
                entity.getFullName(),
                roles
        );
    }
}
