package com.test.backend.automation.domain.model.aggregates;

import com.test.backend.automation.domain.model.entities.CalibrationRecord;
import com.test.backend.labs.domain.model.aggregates.Laboratory;
import com.test.backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
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

    @Column(name = "calibration_date")
    private java.time.LocalDate calibrationDate;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "status")
    private String status = "INACTIVE";

    @Column(name = "last_connected")
    private java.time.LocalDateTime lastConnected;

    @ManyToOne
    @JoinColumn(name = "laboratory_id")
    private Laboratory laboratory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_threshold_id", nullable = true)
    private EquipmentThreshold equipment;

    @Column(name = "min_threshold")
    private Double minThreshold;

    @Column(name = "max_threshold")
    private Double maxThreshold;

    @Column(name = "warning_threshold")
    private Double warningThreshold;

    @OneToMany(mappedBy = "sensorConfiguration", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CalibrationRecord> calibrationRecords = new ArrayList<>();

    public SensorConfiguration(com.test.backend.automation.domain.model.commands.CreateSensorConfigurationCommand command, Laboratory laboratory, EquipmentThreshold equipment) {
        this.sensorName = command.sensorName();
        this.type = command.type();
        this.unit = command.unit();
        this.isActive = command.isActive();
        this.laboratory = laboratory;
        this.equipment = equipment;
        this.minThreshold = command.minThreshold();
        this.maxThreshold = command.maxThreshold();
        this.warningThreshold = command.warningThreshold();
        this.status = "INACTIVE";
        this.lastConnected = null;
        this.calibrationDate = null;
    }

    public SensorConfiguration updateFrom(com.test.backend.automation.domain.model.commands.UpdateSensorConfigurationCommand command, Laboratory laboratory, EquipmentThreshold equipment) {
        this.sensorName = command.sensorName();
        this.type = command.type();
        this.unit = command.unit();
        this.isActive = command.isActive();
        this.laboratory = laboratory;
        this.equipment = equipment;
        this.minThreshold = command.minThreshold();
        this.maxThreshold = command.maxThreshold();
        this.warningThreshold = command.warningThreshold();
        return this;
    }

    public SensorConfiguration calibrate(com.test.backend.automation.domain.model.commands.CalibrateSensorCommand command) {
        CalibrationRecord record = new CalibrationRecord();
        record.setSensorConfiguration(this);
        record.setCertificateId(command.certificateId());
        record.setExpirationDate(command.expirationDate());
        record.setCalibratedAt(command.calibratedAt());
        this.calibrationRecords.add(record);
        this.calibrationDate = command.calibratedAt().toLocalDate();
        return this;
    }
}
