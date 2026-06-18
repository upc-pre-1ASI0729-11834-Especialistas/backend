package com.test.backend.history.domain.services;

import com.test.backend.history.domain.model.aggregates.HistoryRecord;
import com.test.backend.history.domain.model.queries.GetAllHistoryRecordsQuery;
import com.test.backend.history.domain.model.queries.GetHistoryRecordByIdQuery;

import java.util.List;
import java.util.Optional;

public interface HistoryRecordQueryService {
    List<HistoryRecord> handle(GetAllHistoryRecordsQuery query);
    Optional<HistoryRecord> handle(GetHistoryRecordByIdQuery query);
}
