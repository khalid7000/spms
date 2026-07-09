package com.rit.spms.service;

import com.rit.spms.domain.*;
import com.rit.spms.domain.enums.StrategyType;
import com.rit.spms.dto.request.CreateObjectiveRequest;
import com.rit.spms.dto.response.ObjectiveResponse;
import com.rit.spms.exception.BusinessRuleException;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.exception.UnauthorizedException;
import com.rit.spms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ObjectiveService {

    private final ObjectiveRepository objectiveRepository;
    private final GoalRepository goalRepository;
    private final AppUserRepository appUserRepository;
    private final ObjectiveMappingRepository objectiveMappingRepository;
    private final StrategyRepository strategyRepository;
    private final PermissionService permissionService;
    private final AuditService auditService;

    public Objective createObjective(Long goalId, CreateObjectiveRequest req, Long currentUserId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", goalId));
        Long strategyId = goal.getStrategy().getId();
        permissionService.assertCanEditContent(currentUserId, strategyId);

        Strategy strategy = goal.getStrategy();
        boolean isDeptStrategy = strategy.getStrategyType() != StrategyType.UNIVERSITY;

        // Mirrors the check InitiativeService already enforces at initiative-creation time --
        // moved earlier here so a department objective can never be saved unmapped in the first
        // place, rather than only being caught later when someone tries to add an initiative to it.
        if (isDeptStrategy && (req.getUniversityObjectiveIds() == null || req.getUniversityObjectiveIds().isEmpty())) {
            throw new BusinessRuleException("Department objective must be mapped to at least one university objective");
        }

        AppUser creator = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));

        Objective objective = Objective.builder()
                .goal(goal)
                .title(req.getTitle())
                .description(req.getDescription())
                .sortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0)
                .frozen(false)
                .createdBy(creator)
                .build();
        objective = objectiveRepository.save(objective);

        if (isDeptStrategy && req.getUniversityObjectiveIds() != null) {
            for (Long univObjId : req.getUniversityObjectiveIds()) {
                Objective univObj = objectiveRepository.findById(univObjId)
                        .orElseThrow(() -> new ResourceNotFoundException("University Objective", univObjId));
                ObjectiveMapping mapping = ObjectiveMapping.builder()
                        .deptObjective(objective)
                        .universityObjective(univObj)
                        .build();
                objectiveMappingRepository.save(mapping);
            }
        }

        auditService.log(creator, "CREATE_OBJECTIVE", "Objective", objective.getId(), strategy,
                "Created objective: " + objective.getTitle());
        return objective;
    }

    public Objective updateObjective(Long objectiveId, CreateObjectiveRequest req, Long currentUserId) {
        Objective objective = objectiveRepository.findById(objectiveId)
                .orElseThrow(() -> new ResourceNotFoundException("Objective", objectiveId));
        Long strategyId = objective.getGoal().getStrategy().getId();
        boolean isDeptStrategy = objective.getGoal().getStrategy().getStrategyType() != StrategyType.UNIVERSITY;

        if (objective.getFrozen() && !permissionService.isOwner(currentUserId, strategyId)) {
            throw new UnauthorizedException("Only the Owner can modify a frozen objective");
        }
        permissionService.assertCanEditContent(currentUserId, strategyId);

        // Same invariant as createObjective: a department objective can never end up with zero
        // university-objective mappings. The frontend always sends this field for dept strategies,
        // so an explicit empty list here means the user tried to clear every mapping -- reject it
        // rather than silently leaving the objective unmapped.
        if (isDeptStrategy && req.getUniversityObjectiveIds() != null && req.getUniversityObjectiveIds().isEmpty()) {
            throw new BusinessRuleException("Department objective must be mapped to at least one university objective");
        }

        AppUser user = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));
        String oldTitle = objective.getTitle();

        objective.setTitle(req.getTitle());
        if (req.getDescription() != null) objective.setDescription(req.getDescription());
        if (req.getSortOrder() != null) objective.setSortOrder(req.getSortOrder());

        if (req.getUniversityObjectiveIds() != null) {
            // Diff against what's already mapped instead of delete-all-then-reinsert: Hibernate's
            // default flush order runs inserts before deletes, so re-creating a mapping that's
            // kept unchanged (e.g. adding a 2nd university objective alongside an existing one)
            // would insert the "new" row for the unchanged one before its old row is actually
            // deleted, tripping the (dept_objective_id, university_objective_id) unique
            // constraint with "A record with the same key already exists."
            List<ObjectiveMapping> existingMappings = objectiveMappingRepository.findByDeptObjectiveId(objectiveId);
            Set<Long> newUnivObjIds = new HashSet<>(req.getUniversityObjectiveIds());

            for (ObjectiveMapping existing : existingMappings) {
                if (!newUnivObjIds.contains(existing.getUniversityObjective().getId())) {
                    objectiveMappingRepository.delete(existing);
                }
            }

            Set<Long> keptUnivObjIds = existingMappings.stream()
                    .map(m -> m.getUniversityObjective().getId())
                    .collect(Collectors.toSet());
            for (Long univObjId : newUnivObjIds) {
                if (keptUnivObjIds.contains(univObjId)) continue;
                Objective univObj = objectiveRepository.findById(univObjId)
                        .orElseThrow(() -> new ResourceNotFoundException("University Objective", univObjId));
                ObjectiveMapping mapping = ObjectiveMapping.builder()
                        .deptObjective(objective)
                        .universityObjective(univObj)
                        .build();
                objectiveMappingRepository.save(mapping);
            }
        }

        objective = objectiveRepository.save(objective);
        auditService.log(user, "UPDATE_OBJECTIVE", "Objective", objectiveId, objective.getGoal().getStrategy(),
                oldTitle, objective.getTitle(), "Updated objective");
        return objective;
    }

    public void deleteObjective(Long objectiveId, Long currentUserId) {
        Objective objective = objectiveRepository.findById(objectiveId)
                .orElseThrow(() -> new ResourceNotFoundException("Objective", objectiveId));
        Long strategyId = objective.getGoal().getStrategy().getId();

        if (objective.getFrozen() && !permissionService.isOwner(currentUserId, strategyId)) {
            throw new UnauthorizedException("Only the Owner can delete a frozen objective");
        }
        permissionService.assertCanEditContent(currentUserId, strategyId);

        AppUser user = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));
        auditService.log(user, "DELETE_OBJECTIVE", "Objective", objectiveId, objective.getGoal().getStrategy(),
                "Deleted objective: " + objective.getTitle());

        objectiveRepository.delete(objective);
    }

    public Objective setFrozen(Long objectiveId, boolean frozen, Long currentUserId) {
        Objective objective = objectiveRepository.findById(objectiveId)
                .orElseThrow(() -> new ResourceNotFoundException("Objective", objectiveId));
        Long strategyId = objective.getGoal().getStrategy().getId();
        permissionService.assertOwner(currentUserId, strategyId);

        AppUser user = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));

        objective.setFrozen(frozen);
        objective = objectiveRepository.save(objective);
        auditService.log(user, frozen ? "FREEZE_OBJECTIVE" : "UNFREEZE_OBJECTIVE",
                "Objective", objectiveId, objective.getGoal().getStrategy(),
                (frozen ? "Froze" : "Unfroze") + " objective: " + objective.getTitle());
        return objective;
    }

    @Transactional(readOnly = true)
    public List<ObjectiveResponse> getObjectives(Long goalId, Long currentUserId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", goalId));
        permissionService.assertCanRead(currentUserId, goal.getStrategy().getId());

        return objectiveRepository.findByGoalIdOrderBySortOrder(goalId)
                .stream().map(this::toResponse).toList();
    }

    public ObjectiveResponse toResponse(Objective objective) {
        List<Long> univObjIds = objectiveMappingRepository
                .findByDeptObjectiveId(objective.getId())
                .stream().map(om -> om.getUniversityObjective().getId()).toList();

        return ObjectiveResponse.builder()
                .id(objective.getId())
                .goalId(objective.getGoal().getId())
                .title(objective.getTitle())
                .description(objective.getDescription())
                .sortOrder(objective.getSortOrder())
                .frozen(objective.getFrozen())
                .universityObjectiveIds(univObjIds)
                .createdAt(objective.getCreatedAt())
                .updatedAt(objective.getUpdatedAt())
                .build();
    }
}
