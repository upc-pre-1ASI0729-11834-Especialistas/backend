package com.test.backend.history.domain.services;

import com.test.backend.history.domain.model.aggregates.HistoryRecord;
import com.test.backend.history.domain.model.commands.CreateHistoryRecordCommand;
import com.test.backend.history.domain.model.commands.UpdateHistoryRecordCommand;
import com.test.backend.history.domain.model.commands.DeleteHistoryRecordCommand;

import java.util.Optional;

public interface HistoryRecordCommandService {
    Optional<HistoryRecord> handle(CreateHistoryRecordCommand command);
    Optional<HistoryRecord> handle(UpdateHistoryRecordCommand command);
    void handle(DeleteHistoryRecordCommand command);
}
