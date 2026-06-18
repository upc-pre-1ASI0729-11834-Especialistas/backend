package com.test.backend.history.interfaces.rest;

import com.test.backend.history.domain.model.commands.DeleteHistoryRecordCommand;
import com.test.backend.history.domain.model.queries.GetAllHistoryRecordsQuery;
import com.test.backend.history.domain.model.queries.GetHistoryRecordByIdQuery;
import com.test.backend.history.domain.services.HistoryRecordCommandService;
import com.test.backend.history.domain.services.HistoryRecordQueryService;
import com.test.backend.history.interfaces.rest.resources.CreateHistoryRecordResource;
import com.test.backend.history.interfaces.rest.resources.HistoryRecordResource;
import com.test.backend.history.interfaces.rest.resources.UpdateHistoryRecordResource;
import com.test.backend.history.interfaces.rest.transform.CreateHistoryRecordCommandFromResourceAssembler;
import com.test.backend.history.interfaces.rest.transform.HistoryRecordResourceFromEntityAssembler;
import com.test.backend.history.interfaces.rest.transform.UpdateHistoryRecordCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "History Records", description = "History record management API")
@RestController
@RequestMapping("/api/v1/history-records")
public class HistoryRecordController {

    private final HistoryRecordCommandService historyRecordCommandService;
    private final HistoryRecordQueryService historyRecordQueryService;

    public HistoryRecordController(HistoryRecordCommandService historyRecordCommandService,
                                   HistoryRecordQueryService historyRecordQueryService) {
        this.historyRecordCommandService = historyRecordCommandService;
        this.historyRecordQueryService = historyRecordQueryService;
    }

    @GetMapping
    @Operation(summary = "Get all history records")
    public ResponseEntity<List<HistoryRecordResource>> getAllHistoryRecords() {
        var query = new GetAllHistoryRecordsQuery();
        var records = historyRecordQueryService.handle(query);
        var resources = records.stream()
                .map(HistoryRecordResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get history record by ID")
    public ResponseEntity<HistoryRecordResource> getHistoryRecordById(@PathVariable Long id) {
        var query = new GetHistoryRecordByIdQuery(id);
        var result = historyRecordQueryService.handle(query);
        return result.map(record -> ResponseEntity.ok(HistoryRecordResourceFromEntityAssembler.toResourceFromEntity(record)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create a new history record")
    public ResponseEntity<HistoryRecordResource> createHistoryRecord(@RequestBody CreateHistoryRecordResource resource) {
        var command = CreateHistoryRecordCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = historyRecordCommandService.handle(command);
        return result.map(record -> new ResponseEntity<>(HistoryRecordResourceFromEntityAssembler.toResourceFromEntity(record), HttpStatus.CREATED))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing history record")
    public ResponseEntity<HistoryRecordResource> updateHistoryRecord(@PathVariable Long id, @RequestBody UpdateHistoryRecordResource resource) {
        var command = UpdateHistoryRecordCommandFromResourceAssembler.toCommandFromResource(id, resource);
        var result = historyRecordCommandService.handle(command);
        return result.map(record -> ResponseEntity.ok(HistoryRecordResourceFromEntityAssembler.toResourceFromEntity(record)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete history record by ID")
    public ResponseEntity<?> deleteHistoryRecord(@PathVariable Long id) {
        var command = new DeleteHistoryRecordCommand(id);
        try {
            historyRecordCommandService.handle(command);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
