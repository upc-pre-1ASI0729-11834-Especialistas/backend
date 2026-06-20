package com.test.backend.history.application.internal.commandservices;

import com.test.backend.history.domain.model.aggregates.HistoryRecord;
import com.test.backend.history.domain.model.commands.CreateHistoryRecordCommand;
import com.test.backend.history.domain.model.commands.UpdateHistoryRecordCommand;
import com.test.backend.history.domain.model.commands.DeleteHistoryRecordCommand;
import com.test.backend.history.domain.services.HistoryRecordCommandService;
import com.test.backend.history.infrastructure.persistence.jpa.repositories.HistoryRecordRepository;
import com.test.backend.labs.domain.model.aggregates.Laboratory;
import com.test.backend.labs.domain.model.entities.LabActivity;
import com.test.backend.labs.infrastructure.persistence.jpa.repositories.LaboratoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class HistoryRecordCommandServiceImpl implements HistoryRecordCommandService {

    private final HistoryRecordRepository historyRecordRepository;
    private final LaboratoryRepository laboratoryRepository;
    private final com.test.backend.shared.application.CurrentWorkspaceService currentWorkspaceService;

    public HistoryRecordCommandServiceImpl(HistoryRecordRepository historyRecordRepository,
                                           LaboratoryRepository laboratoryRepository,
                                           com.test.backend.shared.application.CurrentWorkspaceService currentWorkspaceService) {
        this.historyRecordRepository = historyRecordRepository;
        this.laboratoryRepository = laboratoryRepository;
        this.currentWorkspaceService = currentWorkspaceService;
    }

    @Override
    @Transactional
    public Optional<HistoryRecord> handle(CreateHistoryRecordCommand command) {
        Long workspaceId = currentWorkspaceService.getCurrentWorkspaceId()
                .orElseThrow(() -> new IllegalStateException("User does not have an active workspace"));

        Laboratory laboratory = findLaboratoryByName(command.lab(), workspaceId);
        var record = new HistoryRecord(command, laboratory);
        historyRecordRepository.save(record);

        if (laboratory != null) {
            LabActivity activity = new LabActivity();
            activity.setLaboratory(laboratory);
            
            String title = "Activity Log";
            String icon = "edit_note";
            if (command.name() != null) {
                if (command.name().startsWith("Incident log")) {
                    title = "Incident log added";
                    icon = "warning";
                } else if (command.name().startsWith("Maintenance log")) {
                    title = "Maintenance log added";
                    icon = "build";
                } else if (command.name().startsWith("Laboratory log")) {
                    title = "Laboratory log added";
                    icon = "edit_note";
                } else {
                    title = command.name().split(" - ")[0] + " added";
                }
            }
            activity.setTitle(title);
            activity.setDescription(command.description());
            
            String formattedTime = "Today, " + new java.text.SimpleDateFormat("h:mm a", java.util.Locale.ENGLISH).format(new java.util.Date());
            activity.setTimestamp(formattedTime);
            activity.setIcon(icon);

            laboratory.getActivities().add(activity);
            laboratoryRepository.save(laboratory);
        }

        return Optional.of(record);
    }

    @Override
    @Transactional
    public Optional<HistoryRecord> handle(UpdateHistoryRecordCommand command) {
        Long workspaceId = currentWorkspaceService.getCurrentWorkspaceId()
                .orElseThrow(() -> new IllegalStateException("User does not have an active workspace"));

        var recordOpt = historyRecordRepository.findByIdAndLaboratoryWorkspaceId(command.id(), workspaceId);
        if (recordOpt.isEmpty()) return Optional.empty();

        var record = recordOpt.get();
        Laboratory laboratory = findLaboratoryByName(command.lab(), workspaceId);
        
        record.updateFrom(command, laboratory);
        historyRecordRepository.save(record);
        return Optional.of(record);
    }

    @Override
    @Transactional
    public void handle(DeleteHistoryRecordCommand command) {
        Long workspaceId = currentWorkspaceService.getCurrentWorkspaceId()
                .orElseThrow(() -> new IllegalStateException("User does not have an active workspace"));

        var record = historyRecordRepository.findByIdAndLaboratoryWorkspaceId(command.id(), workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("HistoryRecord not found in this workspace"));

        historyRecordRepository.delete(record);
    }

    private Laboratory findLaboratoryByName(String labName, Long workspaceId) {
        if (labName == null || labName.isBlank()) return null;
        return laboratoryRepository.findByWorkspaceId(workspaceId).stream()
                .filter(l -> l.getName().equalsIgnoreCase(labName) || l.getLabCode().equalsIgnoreCase(labName))
                .findFirst()
                .orElse(null);
    }
}
