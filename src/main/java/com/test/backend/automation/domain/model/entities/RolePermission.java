package com.test.backend.automation.domain.model.entities;

import com.test.backend.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "role_permissions")
public class RolePermission extends AuditableModel {

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(nullable = false)
    private String permission;

    @Column(name = "is_granted")
    private boolean isGranted;

    @Column(name = "last_audit_date")
    private java.time.LocalDate lastAuditDate;
}
