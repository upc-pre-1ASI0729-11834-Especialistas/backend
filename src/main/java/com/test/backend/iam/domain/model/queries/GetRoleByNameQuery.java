package com.test.backend.iam.domain.model.queries;

import com.test.backend.iam.domain.model.valueobjects.Roles;

public record GetRoleByNameQuery(Roles name) {
}
