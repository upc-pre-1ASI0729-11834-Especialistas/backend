package com.test.backend.automation.infrastructure.persistence.jpa.repositories;

import com.test.backend.automation.domain.model.entities.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
}
