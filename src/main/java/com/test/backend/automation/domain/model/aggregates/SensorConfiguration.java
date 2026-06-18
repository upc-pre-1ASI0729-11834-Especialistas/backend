package com.test.backend.automation.domain.model.aggregates;

import com.test.backend.automation.domain.model.entities.CalibrationRecord;
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
@Table(name = "sensor_configurations")
public class SensorConfiguration extends AuditableAbstractAggregateRoot<SensorConfiguration> {

    @Column(name = "sensor_name", nullable = false)
    private String sensorName;

    private String type;

    private String unit;

    @Temporal(TemporalType.DATE)
    @Column(name = "calibration_date")
    private Date calibrationDate;

    @Column(name = "is_active")
    private boolean isActive;

    @OneToMany(mappedBy = "sensorConfiguration", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CalibrationRecord> calibrationRecords = new ArrayList<>();
}
