package com.test.backend.labs.domain.services;

import com.test.backend.labs.domain.model.aggregates.Laboratory;
import com.test.backend.labs.domain.model.queries.GetAllLaboratoriesQuery;
import com.test.backend.labs.domain.model.queries.GetLaboratoryByIdQuery;

import java.util.List;
import java.util.Optional;

public interface LaboratoryQueryService {
    List<Laboratory> handle(GetAllLaboratoriesQuery query);
    Optional<Laboratory> handle(GetLaboratoryByIdQuery query);
}
