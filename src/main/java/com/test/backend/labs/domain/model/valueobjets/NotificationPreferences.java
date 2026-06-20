package com.test.backend.labs.domain.model.valueobjets;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferences {
    @Column(name = "notif_email")
    private boolean email;

    @Column(name = "notif_sms")
    private boolean sms;

    @Column(name = "notif_push")
    private boolean push;

    @Column(name = "notif_critical_only")
    private boolean criticalOnly;
}
