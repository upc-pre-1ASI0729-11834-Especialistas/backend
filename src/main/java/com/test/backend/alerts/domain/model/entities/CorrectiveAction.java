package com.test.backend.alerts.domain.model.entities;

import com.test.backend.alerts.domain.model.aggregates.Incident;
import com.test.backend.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "corrective_actions")
public class CorrectiveAction extends AuditableModel {

    @OneToOne
    @JoinColumn(name = "incident_id", unique = true, nullable = false)
    private Incident incident;

    @Column(name = "technician_id", nullable = false)
    private Long technicianId;

    @Lob
    @Column(name = "action_taken", columnDefinition = "TEXT")
    private String actionTaken;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "performed_at")
    private Date performedAt;
}
