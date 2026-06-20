package com.test.backend.iam.domain.model.commands;

public record UpdatePasswordCommand(String email, String currentPassword, String newPassword) {
}
