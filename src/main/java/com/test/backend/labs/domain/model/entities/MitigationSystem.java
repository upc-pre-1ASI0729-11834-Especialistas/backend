package com.test.backend.labs.domain.model.entities;

import com.test.backend.labs.domain.model.aggregates.StorageUnit;
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
@Table(name = "mitigation_systems")
public class MitigationSystem extends AuditableModel {

    @OneToOne
    @JoinColumn(name = "storage_unit_id", unique = true, nullable = false)
    private StorageUnit storageUnit;

    private String type;

    @Column(name = "is_active")
    private boolean isActive;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_activated_at")
    private Date lastActivatedAt;
}
