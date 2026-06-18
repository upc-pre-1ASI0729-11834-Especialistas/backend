package com.test.backend.alerts.domain.model.entities;

import com.test.backend.alerts.domain.model.aggregates.Alert;
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
@Table(name = "alert_metrics")
public class AlertMetric extends AuditableModel {

    @ManyToOne
    @JoinColumn(name = "alert_id", nullable = false)
    private Alert alert;

    private String label;

    private String value;
}
