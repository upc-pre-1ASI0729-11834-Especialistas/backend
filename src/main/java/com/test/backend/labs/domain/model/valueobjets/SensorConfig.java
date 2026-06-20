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
public class SensorConfig {
    @Column(name = "sensor_temperature")
    private boolean temperature;

    @Column(name = "sensor_air_quality")
    private boolean airQuality;

    @Column(name = "sensor_ai_detection")
    private boolean aiDetection;

    @Column(name = "sensor_ventilation")
    private boolean ventilation;

    @Column(name = "sensor_air_conditioning")
    private boolean airConditioning;

    @Column(name = "sensor_vibration")
    private boolean vibration;

    @Column(name = "sensor_lighting")
    private boolean lighting;
}
