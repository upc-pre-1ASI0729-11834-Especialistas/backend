package com.test.backend.history.application.internal.queryservices;

import com.test.backend.history.domain.model.aggregates.HistoryRecord;
import com.test.backend.history.domain.model.queries.GetAllHistoryRecordsQuery;
import com.test.backend.history.domain.model.queries.GetHistoryRecordByIdQuery;
import com.test.backend.history.domain.services.HistoryRecordQueryService;
import com.test.backend.history.infrastructure.persistence.jpa.repositories.HistoryRecordRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HistoryRecordQueryServiceImpl implements HistoryRecordQueryService {

    private final HistoryRecordRepository historyRecordRepository;

    public HistoryRecordQueryServiceImpl(HistoryRecordRepository historyRecordRepository) {
        this.historyRecordRepository = historyRecordRepository;
    }

    @Override
    public List<HistoryRecord> handle(GetAllHistoryRecordsQuery query) {
        return historyRecordRepository.findAll();
    }

    @Override
    public Optional<HistoryRecord> handle(GetHistoryRecordByIdQuery query) {
        return historyRecordRepository.findById(query.id());
    }
}
