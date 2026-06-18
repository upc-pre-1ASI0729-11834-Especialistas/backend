package com.test.backend.automation.domain.model.commands;

import java.util.Date;

public record CalibrateSensorCommand(
    Long sensorConfigurationId,
    String certificateId,
    Date expirationDate,
    Date calibratedAt
) {}
