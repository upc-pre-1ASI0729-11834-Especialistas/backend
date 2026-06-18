package com.test.backend.automation.domain.model.entities;

import com.test.backend.automation.domain.model.aggregates.UserProfile;
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
@Table(name = "notification_preferences")
public class NotificationPreference extends AuditableModel {

    @ManyToOne
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;

    private String channel;

    @Column(name = "is_enabled")
    private boolean isEnabled;

    private String threshold;

    private String description;
}
