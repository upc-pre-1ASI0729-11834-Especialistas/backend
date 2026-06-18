package com.test.backend.labs.domain.model.entities;

import com.test.backend.labs.domain.model.aggregates.Laboratory;
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
@Table(name = "lab_metrics")
public class LabMetric extends AuditableModel {

    @ManyToOne
    @JoinColumn(name = "laboratory_id", nullable = false)
    private Laboratory laboratory;

    private String name;

    private String value;

    private String unit;

    private String status;

    private String icon;

    private String sparkline;

    private Double threshold;

    @Column(name = "object_type")
    private String objectType;
}
