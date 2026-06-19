package com.test.backend.history.application.internal.queryservices;

import com.test.backend.history.domain.model.aggregates.HistoryRecord;
import com.test.backend.history.domain.model.queries.GetAllHistoryRecordsQuery;
import com.test.backend.history.domain.model.queries.GetHistoryRecordByIdQuery;
import com.test.backend.history.domain.services.HistoryRecordQueryService;
import com.test.backend.history.infrastructure.persistence.jpa.repositories.HistoryRecordRepository;
import com.test.backend.shared.application.CurrentWorkspaceService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HistoryRecordQueryServiceImpl implements HistoryRecordQueryService {

    private final HistoryRecordRepository historyRecordRepository;
    private final CurrentWorkspaceService currentWorkspaceService;

    public HistoryRecordQueryServiceImpl(HistoryRecordRepository historyRecordRepository,
                                         CurrentWorkspaceService currentWorkspaceService) {
        this.historyRecordRepository = historyRecordRepository;
        this.currentWorkspaceService = currentWorkspaceService;
    }

    @Override
    public List<HistoryRecord> handle(GetAllHistoryRecordsQuery query) {
        var profileOpt = currentWorkspaceService.getCurrentUserProfile();
        if (profileOpt.isEmpty()) {
            return List.of();
        }
        var profile = profileOpt.get();
        var records = historyRecordRepository.findByLaboratoryWorkspaceId(profile.getWorkspaceId());

        if (profile.getRole() != null && "Administrator".equalsIgnoreCase(profile.getRole().getName())) {
            return records;
        }

        var allowedLabIds = profile.getLabAccesses().stream()
                .map(access -> access.getLaboratory().getId())
                .toList();

        return records.stream()
                .filter(record -> record.getLaboratory() != null && allowedLabIds.contains(record.getLaboratory().getId()))
                .toList();
    }

    @Override
    public Optional<HistoryRecord> handle(GetHistoryRecordByIdQuery query) {
        var profileOpt = currentWorkspaceService.getCurrentUserProfile();
        if (profileOpt.isEmpty()) {
            return Optional.empty();
        }
        var profile = profileOpt.get();
        var recordOpt = historyRecordRepository.findByIdAndLaboratoryWorkspaceId(query.id(), profile.getWorkspaceId());
        if (recordOpt.isEmpty()) {
            return Optional.empty();
        }
        var record = recordOpt.get();

        if (profile.getRole() != null && "Administrator".equalsIgnoreCase(profile.getRole().getName())) {
            return Optional.of(record);
        }

        boolean hasAccess = profile.getLabAccesses().stream()
                .anyMatch(access -> record.getLaboratory() != null && access.getLaboratory().getId().equals(record.getLaboratory().getId()));

        return hasAccess ? Optional.of(record) : Optional.empty();
    }
}
