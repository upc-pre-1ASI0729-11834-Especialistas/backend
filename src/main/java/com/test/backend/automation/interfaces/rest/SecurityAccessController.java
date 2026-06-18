package com.test.backend.automation.interfaces.rest;

import com.test.backend.automation.infrastructure.persistence.jpa.repositories.RolePermissionRepository;
import com.test.backend.automation.interfaces.rest.resources.SecurityAccessResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Security Accesses", description = "Security Accesses management API (Role Permissions)")
@RestController
@RequestMapping("/api/v1/security-accesses")
public class SecurityAccessController {

    private final RolePermissionRepository rolePermissionRepository;

    public SecurityAccessController(RolePermissionRepository rolePermissionRepository) {
        this.rolePermissionRepository = rolePermissionRepository;
    }

    @GetMapping
    @Operation(summary = "Get all security accesses (role permissions)")
    public ResponseEntity<List<SecurityAccessResource>> getAllAccesses() {
        var permissions = rolePermissionRepository.findAll();
        var resources = permissions.stream()
                .map(p -> new SecurityAccessResource(
                        p.getId(),
                        p.getPermission(),
                        p.getRole() != null ? p.getRole().getName() : "",
                        p.isGranted(),
                        p.getLastAuditDate()
                ))
                .toList();
        return ResponseEntity.ok(resources);
    }
}
