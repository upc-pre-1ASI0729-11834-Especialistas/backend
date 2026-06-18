package com.test.backend.telemetry.domain.model.aggregates;

import com.test.backend.automation.domain.model.aggregates.SensorConfiguration;
import com.test.backend.labs.domain.model.aggregates.Laboratory;
import com.test.backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.format.DateTimeFormatter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sensor_readings")
public class SensorReading extends AuditableAbstractAggregateRoot<SensorReading> {

    @ManyToOne
    @JoinColumn(name = "sensor_configuration_id", nullable = false)
    private SensorConfiguration sensorConfiguration;

    @ManyToOne
    @JoinColumn(name = "laboratory_id", nullable = false)
    private Laboratory laboratory;

    @Column(name = "metric_key", nullable = false)
    private String metricKey;

    @Column(name = "metric_value", nullable = false)
    private Double metricValue;

    @Column(name = "recorded_at", nullable = false)
    private java.time.LocalDateTime recordedAt;

    // Getter de compatibilidad con código heredado que agrupa por fecha
    public String getDate() {
        if (this.recordedAt == null) return null;
        return this.recordedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // Getter de compatibilidad con código heredado que lee el valor de temperatura
    public Double getValue() {
        return this.metricValue;
    }
}
