package com.test.backend.history.application.internal.commandservices;

import com.test.backend.history.domain.model.aggregates.HistoryRecord;
import com.test.backend.history.domain.model.commands.CreateHistoryRecordCommand;
import com.test.backend.history.domain.model.commands.UpdateHistoryRecordCommand;
import com.test.backend.history.domain.model.commands.DeleteHistoryRecordCommand;
import com.test.backend.history.domain.services.HistoryRecordCommandService;
import com.test.backend.history.infrastructure.persistence.jpa.repositories.HistoryRecordRepository;
import com.test.backend.labs.domain.model.aggregates.Laboratory;
import com.test.backend.labs.infrastructure.persistence.jpa.repositories.LaboratoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class HistoryRecordCommandServiceImpl implements HistoryRecordCommandService {

    private final HistoryRecordRepository historyRecordRepository;
    private final LaboratoryRepository laboratoryRepository;

    public HistoryRecordCommandServiceImpl(HistoryRecordRepository historyRecordRepository,
                                           LaboratoryRepository laboratoryRepository) {
        this.historyRecordRepository = historyRecordRepository;
        this.laboratoryRepository = laboratoryRepository;
    }

    @Override
    @Transactional
    public Optional<HistoryRecord> handle(CreateHistoryRecordCommand command) {
        Laboratory laboratory = findLaboratoryByName(command.lab());
        var record = new HistoryRecord(command, laboratory);
        historyRecordRepository.save(record);
        return Optional.of(record);
    }

    @Override
    @Transactional
    public Optional<HistoryRecord> handle(UpdateHistoryRecordCommand command) {
        var recordOpt = historyRecordRepository.findById(command.id());
        if (recordOpt.isEmpty()) return Optional.empty();

        var record = recordOpt.get();
        Laboratory laboratory = findLaboratoryByName(command.lab());
        
        record.updateFrom(command, laboratory);
        historyRecordRepository.save(record);
        return Optional.of(record);
    }

    @Override
    @Transactional
    public void handle(DeleteHistoryRecordCommand command) {
        if (!historyRecordRepository.existsById(command.id())) {
            throw new IllegalArgumentException("HistoryRecord not found");
        }
        historyRecordRepository.deleteById(command.id());
    }

    private Laboratory findLaboratoryByName(String labName) {
        if (labName == null || labName.isBlank()) return null;
        return laboratoryRepository.findAll().stream()
                .filter(l -> l.getName().equalsIgnoreCase(labName) || l.getLabCode().equalsIgnoreCase(labName))
                .findFirst()
                .orElse(null);
    }
}
