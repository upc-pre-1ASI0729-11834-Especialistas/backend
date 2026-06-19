package com.test.backend.automation.domain.model.aggregates;

import com.test.backend.automation.domain.model.entities.Role;
import com.test.backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pending_invitations")
public class PendingInvitation extends AuditableAbstractAggregateRoot<PendingInvitation> {

    @Column(nullable = false)
    private String email;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "workspace_id", nullable = false)
    private Long workspaceId;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Convert(converter = com.test.backend.automation.infrastructure.persistence.jpa.converters.LongListConverter.class)
    @Column(name = "laboratory_ids", columnDefinition = "TEXT")
    private List<Long> laboratoryIds = new ArrayList<>();

    public PendingInvitation(com.test.backend.automation.domain.model.commands.CreatePendingInvitationCommand command, Role role, Long workspaceId) {
        this.email = command.email();
        this.role = role;
        this.workspaceId = workspaceId;
        this.sentAt = java.time.LocalDateTime.now();
        this.laboratoryIds = command.laboratoryIds() != null ? command.laboratoryIds() : new ArrayList<>();
    }
}
