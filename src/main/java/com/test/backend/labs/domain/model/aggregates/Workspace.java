package com.test.backend.labs.domain.model.aggregates;

import com.test.backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "workspaces")
public class Workspace extends AuditableAbstractAggregateRoot<Workspace> {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    public Workspace(String name) {
        this.name = name;
        this.code = UUID.randomUUID().toString();
    }
}
