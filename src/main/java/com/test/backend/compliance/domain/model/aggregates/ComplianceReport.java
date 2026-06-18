package com.test.backend.compliance.domain.model.aggregates;

import com.test.backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "compliance_reports")
public class ComplianceReport extends AuditableAbstractAggregateRoot<ComplianceReport> {

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "period_start")
    private Date periodStart;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "period_end")
    private Date periodEnd;

    @Column(name = "digital_signature")
    private String digitalSignature;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "generated_at")
    private Date generatedAt;

    @ManyToMany
    @JoinTable(
            name = "report_audit_links",
            joinColumns = @JoinColumn(name = "report_id"),
            inverseJoinColumns = @JoinColumn(name = "audit_id")
    )
    private List<AuditTrail> auditTrails = new ArrayList<>();
}
