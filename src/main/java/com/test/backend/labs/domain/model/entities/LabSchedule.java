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
@Table(name = "lab_schedules")
public class LabSchedule extends AuditableModel {

    @ManyToOne
    @JoinColumn(name = "laboratory_id", nullable = false)
    private Laboratory laboratory;

    private String name;

    @Column(name = "time_range")
    private String timeRange;

    private boolean active;

    private String icon;
}
