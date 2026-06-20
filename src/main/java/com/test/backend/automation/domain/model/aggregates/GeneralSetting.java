package com.test.backend.automation.domain.model.aggregates;

import com.test.backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
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
@Table(name = "general_settings")
public class GeneralSetting extends AuditableAbstractAggregateRoot<GeneralSetting> {

    @Column(name = "setting_key", unique = true, nullable = false)
    private String settingKey;

    private String value;

    private String category;

    private String description;
}
