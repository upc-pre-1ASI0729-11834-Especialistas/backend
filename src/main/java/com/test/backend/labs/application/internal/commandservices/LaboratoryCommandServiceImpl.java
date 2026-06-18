package com.test.backend.labs.application.internal.commandservices;

import com.test.backend.labs.domain.model.aggregates.Laboratory;
import com.test.backend.labs.domain.model.commands.CreateLaboratoryCommand;
import com.test.backend.labs.domain.model.commands.UpdateLaboratoryCommand;
import com.test.backend.labs.domain.model.commands.DeleteLaboratoryCommand;
import com.test.backend.labs.domain.services.LaboratoryCommandService;
import com.test.backend.labs.infrastructure.persistence.jpa.repositories.LaboratoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class LaboratoryCommandServiceImpl implements LaboratoryCommandService {

    private final LaboratoryRepository laboratoryRepository;

    public LaboratoryCommandServiceImpl(LaboratoryRepository laboratoryRepository) {
        this.laboratoryRepository = laboratoryRepository;
    }

    @Override
    @Transactional
    public Optional<Laboratory> handle(CreateLaboratoryCommand command) {
        if (laboratoryRepository.findByLabCode(command.labCode()).isPresent()) {
            throw new IllegalArgumentException("Laboratory with code " + command.labCode() + " already exists");
        }
        var laboratory = new Laboratory(command);
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
