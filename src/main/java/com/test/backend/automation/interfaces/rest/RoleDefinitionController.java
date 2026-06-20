package com.test.backend.automation.interfaces.rest;

import com.test.backend.automation.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.test.backend.automation.interfaces.rest.resources.RoleDefinitionResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Role Definitions", description = "Role Definitions management API")
@RestController
@RequestMapping("/api/v1/role-definitions")
public class RoleDefinitionController {

    private final RoleRepository roleRepository;

    public RoleDefinitionController(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @GetMapping
    @Operation(summary = "Get all role definitions")
    public ResponseEntity<List<RoleDefinitionResource>> getAllRoles() {
        var roles = roleRepository.findAll();
        var resources = roles.stream()
                .map(r -> new RoleDefinitionResource(
                        r.getId(),
                        r.getName(),
                        r.getDescription(),
                        r.getPermissions() != null ? r.getPermissions().size() : 0
                ))
                .toList();
        return ResponseEntity.ok(resources);
    }
}
