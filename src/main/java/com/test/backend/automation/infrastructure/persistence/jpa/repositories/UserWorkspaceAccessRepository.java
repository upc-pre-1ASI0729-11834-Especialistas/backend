package com.test.backend.automation.infrastructure.persistence.jpa.repositories;

import com.test.backend.automation.domain.model.entities.UserWorkspaceAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserWorkspaceAccessRepository extends JpaRepository<UserWorkspaceAccess, Long> {
    List<UserWorkspaceAccess> findByUserProfileId(Long userProfileId);
    List<UserWorkspaceAccess> findByWorkspaceId(Long workspaceId);
    Optional<UserWorkspaceAccess> findByUserProfileIdAndWorkspaceId(Long userProfileId, Long workspaceId);
    Optional<UserWorkspaceAccess> findByUserProfileEmailAndWorkspaceId(String email, Long workspaceId);
}
