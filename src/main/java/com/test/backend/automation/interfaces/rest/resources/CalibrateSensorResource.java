package com.test.backend.automation.interfaces.rest.resources;

import java.util.Date;

public record CalibrateSensorResource(
    String certificateId,
    Date expirationDate,
    Date calibratedAt
) {}
