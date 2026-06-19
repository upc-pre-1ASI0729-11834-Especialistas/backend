package com.test.backend.labs.application.internal.queryservices;

import com.test.backend.labs.domain.model.aggregates.Laboratory;
import com.test.backend.labs.domain.model.queries.GetAllLaboratoriesQuery;
import com.test.backend.labs.domain.model.queries.GetLaboratoryByIdQuery;
import com.test.backend.labs.domain.services.LaboratoryQueryService;
import com.test.backend.labs.infrastructure.persistence.jpa.repositories.LaboratoryRepository;
import com.test.backend.shared.application.CurrentWorkspaceService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LaboratoryQueryServiceImpl implements LaboratoryQueryService {

    private final LaboratoryRepository laboratoryRepository;
    private final CurrentWorkspaceService currentWorkspaceService;

    public LaboratoryQueryServiceImpl(LaboratoryRepository laboratoryRepository,
                                      CurrentWorkspaceService currentWorkspaceService) {
        this.laboratoryRepository = laboratoryRepository;
        this.currentWorkspaceService = currentWorkspaceService;
    }

    @Override
    public List<Laboratory> handle(GetAllLaboratoriesQuery query) {
        var profileOpt = currentWorkspaceService.getCurrentUserProfile();
        if (profileOpt.isEmpty()) {
            return List.of();
        }
        var profile = profileOpt.get();
        var allLabs = laboratoryRepository.findByWorkspaceId(profile.getWorkspaceId());

        if (profile.getRole() != null && "Administrator".equalsIgnoreCase(profile.getRole().getName())) {
            return allLabs;
        }

        var allowedLabIds = profile.getLabAccesses().stream()
                .map(access -> access.getLaboratory().getId())
                .toList();

        return allLabs.stream()
                .filter(lab -> allowedLabIds.contains(lab.getId()))
                .toList();
    }

    @Override
    public Optional<Laboratory> handle(GetLaboratoryByIdQuery query) {
        var profileOpt = currentWorkspaceService.getCurrentUserProfile();
        if (profileOpt.isEmpty()) {
            return Optional.empty();
        }
        var profile = profileOpt.get();
        var labOpt = laboratoryRepository.findByIdAndWorkspaceId(query.id(), profile.getWorkspaceId());
        if (labOpt.isEmpty()) {
            return Optional.empty();
        }
        var lab = labOpt.get();

        if (profile.getRole() != null && "Administrator".equalsIgnoreCase(profile.getRole().getName())) {
            return Optional.of(lab);
        }

        boolean hasAccess = profile.getLabAccesses().stream()
                .anyMatch(access -> access.getLaboratory().getId().equals(lab.getId()));

        return hasAccess ? Optional.of(lab) : Optional.empty();
    }
}
