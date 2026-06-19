package com.test.backend.automation.infrastructure.persistence.jpa.repositories;

import com.test.backend.automation.domain.model.aggregates.PendingInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PendingInvitationRepository extends JpaRepository<PendingInvitation, Long> {
    Optional<PendingInvitation> findByEmail(String email);
    List<PendingInvitation> findAllByEmail(String email);
    List<PendingInvitation> findByWorkspaceId(Long workspaceId);
    Optional<PendingInvitation> findByIdAndWorkspaceId(Long id, Long workspaceId);
}
