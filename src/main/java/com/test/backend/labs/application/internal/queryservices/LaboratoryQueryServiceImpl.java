package com.test.backend.labs.application.internal.queryservices;

import com.test.backend.labs.domain.model.aggregates.Laboratory;
import com.test.backend.labs.domain.model.queries.GetAllLaboratoriesQuery;
import com.test.backend.labs.domain.model.queries.GetLaboratoryByIdQuery;
import com.test.backend.labs.domain.services.LaboratoryQueryService;
import com.test.backend.labs.infrastructure.persistence.jpa.repositories.LaboratoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LaboratoryQueryServiceImpl implements LaboratoryQueryService {

    private final LaboratoryRepository laboratoryRepository;

    public LaboratoryQueryServiceImpl(LaboratoryRepository laboratoryRepository) {
        this.laboratoryRepository = laboratoryRepository;
    }

    @Override
    public List<Laboratory> handle(GetAllLaboratoriesQuery query) {
        return laboratoryRepository.findAll();
    }

    @Override
    public Optional<Laboratory> handle(GetLaboratoryByIdQuery query) {
        return laboratoryRepository.findById(query.id());
    }
}
