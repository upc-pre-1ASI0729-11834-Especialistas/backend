package com.test.backend.automation.domain.model.entities;

import com.test.backend.automation.domain.model.aggregates.SensorConfiguration;
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
@Table(name = "calibration_records")
public class CalibrationRecord extends AuditableModel {

    @ManyToOne
    @JoinColumn(name = "sensor_configuration_id", nullable = false)
    private SensorConfiguration sensorConfiguration;

    @Column(name = "certificate_id")
    private String certificateId;

    @Temporal(TemporalType.DATE)
    @Column(name = "expiration_date")
    private Date expirationDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "calibrated_at")
    private Date calibratedAt;
}
