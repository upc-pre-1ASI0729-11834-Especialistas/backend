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
@Table(name = "lab_activities")
public class LabActivity extends AuditableModel {

    @ManyToOne
    @JoinColumn(name = "laboratory_id", nullable = false)
    private Laboratory laboratory;

    private String title;

    private String description;

    private String timestamp;

    private String icon;
}
