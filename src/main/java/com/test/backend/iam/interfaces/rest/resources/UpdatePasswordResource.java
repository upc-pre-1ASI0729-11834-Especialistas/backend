package com.test.backend.iam.interfaces.rest.resources;

public record UpdatePasswordResource(String currentPassword, String newPassword) {
}
