package com.test.backend.labs.application.internal.commandservices;

import com.test.backend.labs.domain.model.aggregates.Laboratory;
import com.test.backend.labs.domain.model.commands.CreateLaboratoryCommand;
import com.test.backend.labs.domain.model.commands.UpdateLaboratoryCommand;
import com.test.backend.labs.domain.model.commands.DeleteLaboratoryCommand;
import com.test.backend.labs.domain.model.entities.LabMetricSubscription;
import com.test.backend.labs.domain.services.LaboratoryCommandService;
import com.test.backend.labs.infrastructure.persistence.jpa.repositories.LaboratoryRepository;
import com.test.backend.telemetry.infrastructure.persistence.jpa.repositories.MetricTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class LaboratoryCommandServiceImpl implements LaboratoryCommandService {

    private final LaboratoryRepository laboratoryRepository;
    private final MetricTypeRepository metricTypeRepository;

    public LaboratoryCommandServiceImpl(LaboratoryRepository laboratoryRepository,
                                        MetricTypeRepository metricTypeRepository) {
        this.laboratoryRepository = laboratoryRepository;
        this.metricTypeRepository = metricTypeRepository;
    }

    @Override
    @Transactional
    public Optional<Laboratory> handle(CreateLaboratoryCommand command) {
        if (laboratoryRepository.findByLabCode(command.labCode()).isPresent()) {
            throw new IllegalArgumentException("Laboratory with code " + command.labCode() + " already exists");
        }
        var laboratory = new Laboratory(command);

        // Create metric subscriptions
        if (command.metricSubscriptions() != null) {
            for (var subData : command.metricSubscriptions()) {
                var metricType = metricTypeRepository.findById(subData.metricTypeId())
                    .orElseThrow(() -> new IllegalArgumentException("MetricType with id " + subData.metricTypeId() + " not found"));

                var subscription = new LabMetricSubscription();
                subscription.setLaboratory(laboratory);
                subscription.setMetricType(metricType);
                subscription.setMinThreshold(subData.minThreshold());
                subscription.setMaxThreshold(subData.maxThreshold());
                subscription.setActive(true);
                laboratory.getMetricSubscriptions().add(subscription);
            }
        }

        laboratoryRepository.save(laboratory);
        return Optional.of(laboratory);
    }

    @Override
    @Transactional
    public Optional<Laboratory> handle(UpdateLaboratoryCommand command) {
        var result = laboratoryRepository.findById(command.id());
        if (result.isEmpty()) return Optional.empty();

        var laboratory = result.get();
        if (!laboratory.getLabCode().equals(command.labCode()) &&
            laboratoryRepository.findByLabCode(command.labCode()).isPresent()) {
            throw new IllegalArgumentException("Laboratory with code " + command.labCode() + " already exists");
        }

        laboratory.updateFrom(command);

        // Replace metric subscriptions
        if (command.metricSubscriptions() != null) {
            laboratory.getMetricSubscriptions().clear();
            for (var subData : command.metricSubscriptions()) {
                var metricType = metricTypeRepository.findById(subData.metricTypeId())
                    .orElseThrow(() -> new IllegalArgumentException("MetricType with id " + subData.metricTypeId() + " not found"));

                var subscription = new LabMetricSubscription();
                subscription.setLaboratory(laboratory);
                subscription.setMetricType(metricType);
                subscription.setMinThreshold(subData.minThreshold());
                subscription.setMaxThreshold(subData.maxThreshold());
                subscription.setActive(true);
                laboratory.getMetricSubscriptions().add(subscription);
            }
        }

        laboratoryRepository.save(laboratory);
        return Optional.of(laboratory);
    }

    @Override
    @Transactional
    public void handle(DeleteLaboratoryCommand command) {
        if (!laboratoryRepository.existsById(command.id())) {
            throw new IllegalArgumentException("Laboratory not found");
        }
        laboratoryRepository.deleteById(command.id());
    }
}
