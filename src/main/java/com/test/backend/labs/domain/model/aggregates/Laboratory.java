package com.test.backend.labs.domain.model.aggregates;

import com.test.backend.labs.domain.model.entities.*;
import com.test.backend.labs.domain.model.valueobjets.NotificationPreferences;
import com.test.backend.labs.domain.model.valueobjets.SensorConfig;
import com.test.backend.labs.domain.model.valueobjets.SafetyThresholds;
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
@Table(name = "laboratories")
public class Laboratory extends AuditableAbstractAggregateRoot<Laboratory> {

    @Column(nullable = false)
    private String name;

    private String type;

    private String status;

    private String building;

    private String floor;

    @Column(name = "lab_code", unique = true, nullable = false)
    private String labCode;

    @Column(name = "room_number")
    private String roomNumber;

    private String description;

    @Column(name = "overall_status")
    private String overallStatus;

    private boolean active;

    @Column(name = "is_live")
    private boolean isLive;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_update")
    private Date lastUpdate;

    @Temporal(TemporalType.DATE)
    @Column(name = "next_maintenance")
    private Date nextMaintenance;

    @Column(name = "maintenance_days_left")
    private Integer maintenanceDaysLeft;

    @Embedded
    private SensorConfig sensorConfig;

    @Embedded
    private SafetyThresholds safetyThresholds;

    @Embedded
    private NotificationPreferences notificationPreferences;

    @OneToMany(mappedBy = "laboratory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LabMetric> metrics = new ArrayList<>();

    @OneToMany(mappedBy = "laboratory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LabAlert> alerts = new ArrayList<>();

    @OneToMany(mappedBy = "laboratory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LabActivity> activities = new ArrayList<>();

    @OneToMany(mappedBy = "laboratory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LabSchedule> schedules = new ArrayList<>();

    @OneToMany(mappedBy = "laboratory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StorageUnit> storageUnits = new ArrayList<>();
}
