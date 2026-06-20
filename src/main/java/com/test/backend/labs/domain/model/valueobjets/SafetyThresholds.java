package com.test.backend.labs.domain.model.valueobjets;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SafetyThresholds {
    @Column(name = "threshold_temp_min")
    private Double tempMin;

    @Column(name = "threshold_temp_max")
    private Double tempMax;

    @Column(name = "threshold_max_co2_ppm")
    private Integer maxCo2Ppm;

    @Column(name = "threshold_gas_sensitivity")
    private String gasSensitivity;

    @Column(name = "threshold_max_vibration")
    private Double maxVibration;

    @Column(name = "threshold_alert_escalation")
    private String alertEscalation;
}
