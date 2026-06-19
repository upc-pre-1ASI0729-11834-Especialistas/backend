package com.test.backend.telemetry.application.internal.queryservices;

import com.test.backend.telemetry.domain.model.aggregates.SensorReading;
import com.test.backend.telemetry.domain.model.queries.GetAllSensorReadingsQuery;
import com.test.backend.telemetry.domain.model.queries.GetSensorReadingByIdQuery;
import com.test.backend.telemetry.domain.services.SensorReadingQueryService;
import com.test.backend.telemetry.infrastructure.persistence.jpa.repositories.SensorReadingRepository;
import com.test.backend.shared.application.CurrentWorkspaceService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SensorReadingQueryServiceImpl implements SensorReadingQueryService {

    private final SensorReadingRepository sensorReadingRepository;
    private final CurrentWorkspaceService currentWorkspaceService;

    public SensorReadingQueryServiceImpl(SensorReadingRepository sensorReadingRepository,
                                         CurrentWorkspaceService currentWorkspaceService) {
        this.sensorReadingRepository = sensorReadingRepository;
        this.currentWorkspaceService = currentWorkspaceService;
    }

    @Override
    public List<SensorReading> handle(GetAllSensorReadingsQuery query) {
        var profileOpt = currentWorkspaceService.getCurrentUserProfile();
        if (profileOpt.isEmpty()) {
            return List.of();
        }
        var profile = profileOpt.get();
        var readings = sensorReadingRepository.findByLaboratoryWorkspaceId(profile.getWorkspaceId());

        if (profile.getRole() != null && "Administrator".equalsIgnoreCase(profile.getRole().getName())) {
            return readings;
        }

        var allowedLabIds = profile.getLabAccesses().stream()
                .map(access -> access.getLaboratory().getId())
                .toList();

        return readings.stream()
                .filter(reading -> reading.getLaboratory() != null && allowedLabIds.contains(reading.getLaboratory().getId()))
                .toList();
    }

    @Override
    public Optional<SensorReading> handle(GetSensorReadingByIdQuery query) {
        var profileOpt = currentWorkspaceService.getCurrentUserProfile();
        if (profileOpt.isEmpty()) {
            return Optional.empty();
        }
        var profile = profileOpt.get();
        var readingOpt = sensorReadingRepository.findByIdAndLaboratoryWorkspaceId(query.id(), profile.getWorkspaceId());
        if (readingOpt.isEmpty()) {
            return Optional.empty();
        }
        var reading = readingOpt.get();

        if (profile.getRole() != null && "Administrator".equalsIgnoreCase(profile.getRole().getName())) {
            return Optional.of(reading);
        }

        boolean hasAccess = profile.getLabAccesses().stream()
                .anyMatch(access -> reading.getLaboratory() != null && access.getLaboratory().getId().equals(reading.getLaboratory().getId()));

        return hasAccess ? Optional.of(reading) : Optional.empty();
    }
}
