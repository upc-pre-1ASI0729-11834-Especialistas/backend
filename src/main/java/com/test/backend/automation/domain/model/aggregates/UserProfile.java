package com.test.backend.automation.domain.model.aggregates;

import com.test.backend.automation.domain.model.entities.Role;
import com.test.backend.automation.domain.model.entities.NotificationPreference;
import com.test.backend.automation.domain.model.entities.LabUserAccess;
import com.test.backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_profiles")
public class UserProfile extends AuditableAbstractAggregateRoot<UserProfile> {

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "professional_title")
    private String professionalTitle;

    @Column(name = "employee_id", unique = true)
    private String employeeId;

    @Column(name = "system_state")
    private String systemState;

    @Column(name = "access_tier")
    private String accessTier;

    @Column(name = "default_start_shift")
    private String defaultStartShift;

    @Column(name = "shift_duration")
    private String shiftDuration;

    @Column(name = "auto_generate_shift_report")
    private boolean autoGenerateShiftReport;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_login")
    private Date lastLogin;

    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NotificationPreference> notificationPreferences = new ArrayList<>();

    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LabUserAccess> labAccesses = new ArrayList<>();
}
