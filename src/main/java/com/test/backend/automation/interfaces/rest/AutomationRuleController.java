package com.test.backend.automation.interfaces.rest;

import com.test.backend.automation.domain.model.commands.DeleteAutomationRuleCommand;
import com.test.backend.automation.domain.model.queries.GetAllAutomationRulesQuery;
import com.test.backend.automation.domain.services.AutomationRuleCommandService;
import com.test.backend.automation.domain.services.AutomationRuleQueryService;
import com.test.backend.automation.interfaces.rest.resources.CreateAutomationRuleResource;
import com.test.backend.automation.interfaces.rest.resources.AutomationRuleResource;
import com.test.backend.automation.interfaces.rest.resources.UpdateAutomationRuleResource;
import com.test.backend.automation.interfaces.rest.transform.CreateAutomationRuleCommandFromResourceAssembler;
import com.test.backend.automation.interfaces.rest.transform.AutomationRuleResourceFromEntityAssembler;
import com.test.backend.automation.interfaces.rest.transform.UpdateAutomationRuleCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Automation Rules", description = "Automation rule management API")
@RestController
@RequestMapping("/api/v1/automation-rules")
public class AutomationRuleController {

    private final AutomationRuleCommandService automationRuleCommandService;
    private final AutomationRuleQueryService automationRuleQueryService;

    public AutomationRuleController(AutomationRuleCommandService automationRuleCommandService,
                                    AutomationRuleQueryService automationRuleQueryService) {
        this.automationRuleCommandService = automationRuleCommandService;
        this.automationRuleQueryService = automationRuleQueryService;
    }

    @GetMapping
    @Operation(summary = "Get all automation rules")
    public ResponseEntity<List<AutomationRuleResource>> getAllRules() {
        var query = new GetAllAutomationRulesQuery();
        var rules = automationRuleQueryService.handle(query);
        var resources = rules.stream()
                .map(AutomationRuleResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get automation rule by ID")
    public ResponseEntity<AutomationRuleResource> getRuleById(@PathVariable Long id) {
        var result = automationRuleQueryService.handle(id);
        return result.map(rule -> ResponseEntity.ok(AutomationRuleResourceFromEntityAssembler.toResourceFromEntity(rule)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create a new automation rule")
    public ResponseEntity<AutomationRuleResource> createRule(@RequestBody CreateAutomationRuleResource resource) {
        var command = CreateAutomationRuleCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = automationRuleCommandService.handle(command);
        return result.map(rule -> new ResponseEntity<>(AutomationRuleResourceFromEntityAssembler.toResourceFromEntity(rule), HttpStatus.CREATED))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing automation rule")
    public ResponseEntity<AutomationRuleResource> updateRule(@PathVariable Long id, @RequestBody UpdateAutomationRuleResource resource) {
        var command = UpdateAutomationRuleCommandFromResourceAssembler.toCommandFromResource(id, resource);
        var result = automationRuleCommandService.handle(command);
        return result.map(rule -> ResponseEntity.ok(AutomationRuleResourceFromEntityAssembler.toResourceFromEntity(rule)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete automation rule by ID")
    public ResponseEntity<?> deleteRule(@PathVariable Long id) {
        var command = new DeleteAutomationRuleCommand(id);
        try {
            automationRuleCommandService.handle(command);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
