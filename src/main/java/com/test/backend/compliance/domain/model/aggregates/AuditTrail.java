package com.test.backend.compliance.domain.model.aggregates;

import com.test.backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
    name = "audit_trails",
    indexes = {
        @Index(name = "idx_audit_trails_target_id", columnList = "target_id")
    }
)
public class AuditTrail extends AuditableAbstractAggregateRoot<AuditTrail> {

    @Column(name = "target_id")
    private String targetId;

    private String action;

    private String signature;

    @ManyToMany(mappedBy = "auditTrails")
    private List<ComplianceReport> complianceReports = new ArrayList<>();
}
