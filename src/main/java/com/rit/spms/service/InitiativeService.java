package com.rit.spms.service;

import com.rit.spms.domain.*;
import com.rit.spms.domain.enums.StrategyState;
import com.rit.spms.domain.enums.StrategyType;
import com.rit.spms.dto.request.CreateInitiativeRequest;
import com.rit.spms.dto.response.InitiativeResponse;
import com.rit.spms.dto.response.MeasurementResponse;
import com.rit.spms.exception.BusinessRuleException;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.exception.UnauthorizedException;
import com.rit.spms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InitiativeService {

    private final InitiativeRepository initiativeRepository;
    private final ObjectiveRepository objectiveRepository;
    private final AppUserRepository appUserRepository;
    private final InitiativeMappingRepository initiativeMappingRepository;
    private final ObjectiveMappingRepository objectiveMappingRepository;
    private final MeasurementRepository measurementRepository;
    private final PermissionService permissionService;
    private final AuditService auditService;

    public Initiative createInitiative(Long objectiveId, CreateInitiativeRequest req, Long currentUserId) {
        Objective objective = objectiveRepository.findById(objectiveId)
                .orElseThrow(() -> new ResourceNotFoundException("Objective", objectiveId));

        Strategy strategy = objective.getGoal().getStrategy();
        Long strategyId = strategy.getId();

        if (!permissionService.canAddInitiative(currentUserId, strategyId)) {
            throw new UnauthorizedException("Cannot add initiatives in the current strategy state");
        }

        if (objective.getFrozen() && !permissionService.isOwner(currentUserId, strategyId)) {
            throw new UnauthorizedException("Only the Owner can add initiatives to a frozen objective");
        }

        boolean isDeptStrategy = strategy.getStrategyType() == StrategyType.DEPARTMENT;

        if (isDeptStrategy) {
            long mappingCount = objectiveMappingRepository.countByDeptObjectiveId(objectiveId);
            if (mappingCount == 0) {
                throw new BusinessRuleException(
                        "Department objective must be mapped to at least one university objective before adding initiatives");
            }
            if (req.getUniversityInitiativeId() == null) {
                throw new BusinessRuleException("Department initiative must map to exactly one university initiative");
            }
            validateUniversityInitiativeForObjective(objectiveId, req.getUniversityInitiativeId());
        }

        AppUser creator = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));

        Initiative initiative = Initiative.builder()
                .objective(objective)
                .title(req.getTitle())
                .description(req.getDescription())
                .sortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0)
                .createdBy(creator)
                .build();
        initiative = initiativeRepository.save(initiative);

        if (isDeptStrategy && req.getUniversityInitiativeId() != null) {
            Initiative univInit = initiativeRepository.findById(req.getUniversityInitiativeId())
                    .orElseThrow(() -> new ResourceNotFoundException("University Initiative", req.getUniversityInitiativeId()));
            InitiativeMapping mapping = InitiativeMapping.builder()
                    .deptInitiative(initiative)
                    .universityInitiative(univInit)
                    .build();
            initiativeMappingRepository.save(mapping);
        }

        auditService.log(creator, "CREATE_INITIATIVE", "Initiative", initiative.getId(), strategy,
                "Created initiative: " + initiative.getTitle());
        return initiative;
    }

    public Initiative updateInitiative(Long initiativeId, CreateInitiativeRequest req, Long currentUserId) {
        Initiative initiative = initiativeRepository.findById(initiativeId)
                .orElseThrow(() -> new ResourceNotFoundException("Initiative", initiativeId));

        Strategy strategy = initiative.getObjective().getGoal().getStrategy();
        Long strategyId = strategy.getId();
        StrategyState state = strategy.getState();

        if (state == StrategyState.DEPLOYED || state == StrategyState.FROZEN) {
            if (!permissionService.isOwner(currentUserId, strategyId)) {
                throw new UnauthorizedException("Plan content cannot be edited in " + state + " state");
            }
        } else {
            if (!permissionService.canEdit(currentUserId, strategyId)) {
                throw new UnauthorizedException("You do not have edit access to this strategy");
            }
        }

        if (initiative.getObjective().getFrozen() && !permissionService.isOwner(currentUserId, strategyId)) {
            throw new UnauthorizedException("Only the Owner can modify initiatives on a frozen objective");
        }

        AppUser user = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));

        String oldTitle = initiative.getTitle();
        initiative.setTitle(req.getTitle());
        if (req.getDescription() != null) initiative.setDescription(req.getDescription());
        if (req.getSortOrder() != null) initiative.setSortOrder(req.getSortOrder());
        initiative = initiativeRepository.save(initiative);

        auditService.log(user, "UPDATE_INITIATIVE", "Initiative", initiativeId, strategy,
                oldTitle, initiative.getTitle(), "Updated initiative");
        return initiative;
    }

    public void deleteInitiative(Long initiativeId, Long currentUserId) {
        Initiative initiative = initiativeRepository.findById(initiativeId)
                .orElseThrow(() -> new ResourceNotFoundException("Initiative", initiativeId));

        Strategy strategy = initiative.getObjective().getGoal().getStrategy();
        Long strategyId = strategy.getId();

        if (strategy.getState() == StrategyState.REVIEW) {
            throw new BusinessRuleException("Initiatives cannot be deleted in REVIEW state");
        }

        permissionService.assertCanEditContent(currentUserId, strategyId);

        if (initiative.getObjective().getFrozen() && !permissionService.isOwner(currentUserId, strategyId)) {
            throw new UnauthorizedException("Only the Owner can delete initiatives on a frozen objective");
        }

        AppUser user = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));

        auditService.log(user, "DELETE_INITIATIVE", "Initiative", initiativeId, strategy,
                "Deleted initiative: " + initiative.getTitle());
        initiativeRepository.delete(initiative);
    }

    @Transactional(readOnly = true)
    public List<InitiativeResponse> getInitiatives(Long objectiveId, Long currentUserId) {
        Objective objective = objectiveRepository.findById(objectiveId)
                .orElseThrow(() -> new ResourceNotFoundException("Objective", objectiveId));
        permissionService.assertCanRead(currentUserId, objective.getGoal().getStrategy().getId());

        return initiativeRepository.findByObjectiveIdOrderBySortOrder(objectiveId)
                .stream().map(this::toResponse).toList();
    }

    public InitiativeResponse toResponse(Initiative initiative) {
        Long univInitId = initiativeMappingRepository.findByDeptInitiativeId(initiative.getId())
                .map(im -> im.getUniversityInitiative().getId())
                .orElse(null);

        List<MeasurementResponse> measurements = measurementRepository
                .findByInitiativeIdOrderBySortOrder(initiative.getId())
                .stream().map(m -> MeasurementResponse.builder()
                        .id(m.getId())
                        .initiativeId(m.getInitiative().getId())
                        .description(m.getDescription())
                        .unit(m.getUnit())
                        .targetValue(m.getTargetValue())
                        .actualValue(m.getActualValue())
                        .sortOrder(m.getSortOrder())
                        .createdAt(m.getCreatedAt())
                        .updatedAt(m.getUpdatedAt())
                        .build()).toList();

        return InitiativeResponse.builder()
                .id(initiative.getId())
                .objectiveId(initiative.getObjective().getId())
                .title(initiative.getTitle())
                .description(initiative.getDescription())
                .sortOrder(initiative.getSortOrder())
                .universityInitiativeId(univInitId)
                .createdAt(initiative.getCreatedAt())
                .updatedAt(initiative.getUpdatedAt())
                .measurements(measurements)
                .build();
    }

    private void validateUniversityInitiativeForObjective(Long deptObjectiveId, Long univInitiativeId) {
        List<Long> univObjIds = objectiveMappingRepository.findByDeptObjectiveId(deptObjectiveId)
                .stream().map(om -> om.getUniversityObjective().getId()).toList();

        if (univObjIds.isEmpty()) {
            throw new BusinessRuleException("Department objective has no university objective mappings");
        }

        Initiative univInit = initiativeRepository.findById(univInitiativeId)
                .orElseThrow(() -> new ResourceNotFoundException("University Initiative", univInitiativeId));

        Long univInitObjId = univInit.getObjective().getId();
        if (!univObjIds.contains(univInitObjId)) {
            throw new BusinessRuleException(
                    "The selected university initiative does not belong to any of the mapped university objectives");
        }
    }
}
