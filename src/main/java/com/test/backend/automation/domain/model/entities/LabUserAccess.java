package com.test.backend.automation.domain.model.entities;

import com.test.backend.automation.domain.model.aggregates.UserProfile;
import com.test.backend.labs.domain.model.aggregates.Laboratory;
import com.test.backend.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "lab_user_accesses")
public class LabUserAccess extends AuditableModel {

    @ManyToOne
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;

    @ManyToOne
    @JoinColumn(name = "laboratory_id", nullable = false)
    private Laboratory laboratory;
}
