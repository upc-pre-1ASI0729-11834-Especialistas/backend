package com.test.backend.labs.domain.services;

import com.test.backend.labs.domain.model.aggregates.Laboratory;
import com.test.backend.labs.domain.model.commands.CreateLaboratoryCommand;
import com.test.backend.labs.domain.model.commands.UpdateLaboratoryCommand;
import com.test.backend.labs.domain.model.commands.DeleteLaboratoryCommand;

import java.util.Optional;

public interface LaboratoryCommandService {
    Optional<Laboratory> handle(CreateLaboratoryCommand command);
    Optional<Laboratory> handle(UpdateLaboratoryCommand command);
    void handle(DeleteLaboratoryCommand command);
}
