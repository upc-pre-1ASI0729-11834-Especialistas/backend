package com.test.backend.labs.domain.model.aggregates;

import com.test.backend.labs.domain.model.entities.MitigationSystem;
import com.test.backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
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
@Table(name = "storage_units")
public class StorageUnit extends AuditableAbstractAggregateRoot<StorageUnit> {

    @ManyToOne
    @JoinColumn(name = "laboratory_id", nullable = false)
    private Laboratory laboratory;

    private String type;

    @Column(name = "loc_room")
    private String locRoom;

    @Column(name = "loc_rack")
    private String locRack;

    @Column(name = "loc_shelf")
    private String locShelf;

    @Column(name = "threshold_min")
    private Double thresholdMin;

    @Column(name = "threshold_max")
    private Double thresholdMax;

    @OneToOne(mappedBy = "storageUnit", cascade = CascadeType.ALL)
    private MitigationSystem mitigationSystem;
}
