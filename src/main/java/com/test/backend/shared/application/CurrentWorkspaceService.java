package com.test.backend.shared.application;

import com.test.backend.automation.infrastructure.persistence.jpa.repositories.UserProfileRepository;
import com.test.backend.automation.domain.model.aggregates.UserProfile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CurrentWorkspaceService {

    private final UserProfileRepository userProfileRepository;

    public CurrentWorkspaceService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    /**
     * Resolves the email of the currently authenticated user.
     */
    public Optional<String> getCurrentUserEmail() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return Optional.empty();
        }
        return Optional.of(authentication.getName());
    }

    /**
     * Resolves the workspace ID of the currently authenticated user.
     */
    public Optional<Long> getCurrentWorkspaceId() {
        return getCurrentUserEmail()
                .flatMap(userProfileRepository::findByEmail)
                .map(profile -> profile.getWorkspaceId());
    }

    /**
     * Resolves the UserProfile of the currently authenticated user.
     */
    public Optional<UserProfile> getCurrentUserProfile() {
        return getCurrentUserEmail()
                .flatMap(userProfileRepository::findByEmail);
    }

    /**
     * Checks if the currently authenticated user has the Administrator role.
     */
    public boolean isCurrentUserAdmin() {
        return getCurrentUserProfile()
                .map(profile -> profile.getRole() != null && "Administrator".equalsIgnoreCase(profile.getRole().getName()))
                .orElse(false);
    }
}
