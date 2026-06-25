package com.rit.spms.service;

import com.rit.spms.domain.*;
import com.rit.spms.domain.enums.RoleType;
import com.rit.spms.domain.enums.StrategyState;
import com.rit.spms.domain.enums.StrategyType;
import com.rit.spms.dto.request.ChangeStateRequest;
import com.rit.spms.dto.request.CreateStrategyRequest;
import com.rit.spms.dto.request.RoleAssignmentRequest;
import com.rit.spms.dto.request.SetThresholdRequest;
import com.rit.spms.dto.response.*;
import com.rit.spms.exception.BusinessRuleException;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StrategyService {

    private final StrategyRepository strategyRepository;
    private final PlanningCycleRepository planningCycleRepository;
    private final DepartmentRepository departmentRepository;
    private final AppUserRepository appUserRepository;
    private final RoleAssignmentRepository roleAssignmentRepository;
    private final GoalRepository goalRepository;
    private final ObjectiveRepository objectiveRepository;
    private final ObjectiveMappingRepository objectiveMappingRepository;
    private final InitiativeRepository initiativeRepository;
    private final InitiativeMappingRepository initiativeMappingRepository;
    private final MeasurementRepository measurementRepository;
    private final VisionAreaRepository visionAreaRepository;
    private final PermissionService permissionService;
    private final AuditService auditService;
    private final ApprovalService approvalService;

    public Strategy createUniversityStrategy(CreateStrategyRequest req, Long currentUserId) {
        PlanningCycle cycle = planningCycleRepository.findById(req.getPlanningCycleId())
                .orElseThrow(() -> new ResourceNotFoundException("PlanningCycle", req.getPlanningCycleId()));

        if (strategyRepository.findByPlanningCycleIdAndDepartmentIsNull(req.getPlanningCycleId()).isPresent()) {
            throw new BusinessRuleException("A university strategy already exists for this planning cycle");
        }

        Strategy strategy = Strategy.builder()
                .planningCycle(cycle)
                .strategyType(StrategyType.UNIVERSITY)
                .state(StrategyState.CREATION)
                .title(req.getTitle())
                .description(req.getDescription())
                .build();
        strategy = strategyRepository.save(strategy);

        AppUser creator = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));
        RoleAssignment ownerRole = RoleAssignment.builder()
                .user(creator)
                .strategy(strategy)
                .role(RoleType.OWNER)
                .build();
        roleAssignmentRepository.save(ownerRole);

        auditService.log(creator, "CREATE_STRATEGY", "Strategy", strategy.getId(), strategy,
                "Created university strategy: " + strategy.getTitle());
        return strategy;
    }

    public Strategy createDepartmentStrategy(CreateStrategyRequest req, Long currentUserId) {
        PlanningCycle cycle = planningCycleRepository.findById(req.getPlanningCycleId())
                .orElseThrow(() -> new ResourceNotFoundException("PlanningCycle", req.getPlanningCycleId()));
        Department dept = departmentRepository.findById(req.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", req.getDepartmentId()));

        if (strategyRepository.findByPlanningCycleIdAndDepartmentId(req.getPlanningCycleId(), req.getDepartmentId()).isPresent()) {
            throw new BusinessRuleException("A strategy for this department and planning cycle already exists");
        }

        Strategy strategy = Strategy.builder()
                .planningCycle(cycle)
                .department(dept)
                .strategyType(StrategyType.DEPARTMENT)
                .state(StrategyState.CREATION)
                .title(req.getTitle())
                .description(req.getDescription())
                .build();
        strategy = strategyRepository.save(strategy);

        AppUser creator = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));
        RoleAssignment ownerRole = RoleAssignment.builder()
                .user(creator)
                .strategy(strategy)
                .role(RoleType.OWNER)
                .build();
        roleAssignmentRepository.save(ownerRole);

        auditService.log(creator, "CREATE_STRATEGY", "Strategy", strategy.getId(), strategy,
                "Created department strategy: " + strategy.getTitle());
        return strategy;
    }

    @Transactional(readOnly = true)
    public StrategyResponse getStrategy(Long strategyId, Long currentUserId) {
        permissionService.assertCanRead(currentUserId, strategyId);
        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy", strategyId));
        return buildStrategyResponse(strategy, true);
    }

    public Strategy changeState(Long strategyId, ChangeStateRequest req, Long currentUserId) {
        permissionService.assertOwner(currentUserId, strategyId);
        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy", strategyId));

        validateStateTransition(strategy.getState(), req.getNewState());

        if (req.getNewState() == StrategyState.CREATION) {
            boolean hasOwner = roleAssignmentRepository.existsByStrategyIdAndRole(strategyId, RoleType.OWNER);
            if (!hasOwner) {
                throw new BusinessRuleException("Strategy must have at least one OWNER before entering CREATION state");
            }
        }

        AppUser user = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));
        String oldState = strategy.getState().name();

        // Requesting DEPLOYED → route through approval chain instead of direct transition
        if (req.getNewState() == StrategyState.DEPLOYED) {
            approvalService.initiateApproval(strategy, user);
            auditService.log(user, "REQUEST_DEPLOY", "Strategy", strategyId, strategy,
                    oldState, strategy.getState().name(),
                    "Deployment requested; state → " + strategy.getState());
            return strategy;
        }

        strategy.setState(req.getNewState());
        strategy = strategyRepository.save(strategy);
        auditService.log(user, "CHANGE_STATE", "Strategy", strategyId, strategy,
                oldState, req.getNewState().name(), "State changed from " + oldState + " to " + req.getNewState());
        return strategy;
    }

    private void validateStateTransition(StrategyState current, StrategyState next) {
        boolean valid = switch (current) {
            case CREATION -> next == StrategyState.REVIEW;
            case REVIEW -> next == StrategyState.CREATION || next == StrategyState.DEPLOYED;
            case APPROVAL_PENDING -> false;  // only the approval service can advance this
            case DEPLOYED -> next == StrategyState.FROZEN;
            case FROZEN -> true;
        };
        if (!valid) {
            throw new BusinessRuleException("Invalid state transition from " + current + " to " + next);
        }
    }

    public RoleAssignment assignRole(Long strategyId, RoleAssignmentRequest req, Long currentUserId) {
        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy", strategyId));
        AppUser currentUser = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));
        AppUser targetUser = appUserRepository.findById(req.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", req.getUserId()));

        // Each strategy must have at most one OWNER
        if (req.getRole() == RoleType.OWNER) {
            roleAssignmentRepository.findByStrategyId(strategyId).stream()
                    .filter(ra -> ra.getRole() == RoleType.OWNER
                            && !ra.getUser().getId().equals(req.getUserId()))
                    .findFirst()
                    .ifPresent(conflict -> {
                        throw new BusinessRuleException(
                                "Strategy already has an Owner (" + conflict.getUser().getEmail()
                                + "). Change or remove the existing Owner first.");
                    });
        }

        RoleAssignment existing = roleAssignmentRepository
                .findByUserIdAndStrategyId(req.getUserId(), strategyId)
                .orElse(null);

        if (existing != null) {
            existing.setRole(req.getRole());
            RoleAssignment saved = roleAssignmentRepository.save(existing);
            auditService.log(currentUser, "ASSIGN_ROLE", "RoleAssignment", saved.getId(), strategy,
                    "Assigned role " + req.getRole() + " to user " + targetUser.getEmail());
            return saved;
        }

        RoleAssignment assignment = RoleAssignment.builder()
                .user(targetUser)
                .strategy(strategy)
                .role(req.getRole())
                .build();
        assignment = roleAssignmentRepository.save(assignment);
        auditService.log(currentUser, "ASSIGN_ROLE", "RoleAssignment", assignment.getId(), strategy,
                "Assigned role " + req.getRole() + " to user " + targetUser.getEmail());
        return assignment;
    }

    @Transactional(readOnly = true)
    public List<DashboardResponse> getDashboard(Long currentUserId) {
        List<RoleAssignment> assignments = roleAssignmentRepository.findByUserId(currentUserId);
        return assignments.stream().map(ra -> {
            Strategy s = ra.getStrategy();
            return DashboardResponse.builder()
                    .strategyId(s.getId())
                    .strategyTitle(s.getTitle())
                    .strategyType(s.getStrategyType())
                    .state(s.getState())
                    .role(ra.getRole())
                    .planningCycleName(s.getPlanningCycle().getName())
                    .departmentName(s.getDepartment() != null ? s.getDepartment().getName() : null)
                    .build();
        }).toList();
    }

    public Strategy setThreshold(Long strategyId, SetThresholdRequest req, Long currentUserId) {
        permissionService.assertOwner(currentUserId, strategyId);
        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy", strategyId));
        strategy.setAchievementThreshold(req.getThreshold());
        strategy = strategyRepository.save(strategy);
        AppUser user = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));
        auditService.log(user, "SET_THRESHOLD", "Strategy", strategyId, strategy,
                "Set achievement threshold to " + req.getThreshold());
        return strategy;
    }

    public StrategyResponse buildStrategyResponse(Strategy strategy, boolean includeChildren) {
        List<VisionAreaResponse> areas = visionAreaRepository
                .findByStrategyIdOrderBySortOrder(strategy.getId())
                .stream().map(a -> VisionAreaResponse.builder()
                        .id(a.getId())
                        .strategyId(a.getStrategy().getId())
                        .name(a.getName())
                        .sortOrder(a.getSortOrder())
                        .createdAt(a.getCreatedAt())
                        .updatedAt(a.getUpdatedAt())
                        .build()).toList();

        List<GoalResponse> goals = null;
        if (includeChildren) {
            goals = goalRepository.findByStrategyIdOrderBySortOrder(strategy.getId())
                    .stream().map(this::buildGoalResponse).toList();
        }
        return StrategyResponse.builder()
                .id(strategy.getId())
                .planningCycleId(strategy.getPlanningCycle().getId())
                .planningCycleName(strategy.getPlanningCycle().getName())
                .departmentId(strategy.getDepartment() != null ? strategy.getDepartment().getId() : null)
                .departmentName(strategy.getDepartment() != null ? strategy.getDepartment().getName() : null)
                .strategyType(strategy.getStrategyType())
                .state(strategy.getState())
                .title(strategy.getTitle())
                .description(strategy.getDescription())
                .achievementThreshold(strategy.getAchievementThreshold())
                .createdAt(strategy.getCreatedAt())
                .updatedAt(strategy.getUpdatedAt())
                .areas(areas)
                .goals(goals)
                .build();
    }

    private GoalResponse buildGoalResponse(com.rit.spms.domain.Goal goal) {
        List<ObjectiveResponse> objectives = objectiveRepository
                .findByGoalIdOrderBySortOrder(goal.getId())
                .stream().map(this::buildObjectiveResponse).toList();
        return GoalResponse.builder()
                .id(goal.getId())
                .strategyId(goal.getStrategy().getId())
                .themeId(goal.getTheme() != null ? goal.getTheme().getId() : null)
                .themeName(goal.getTheme() != null ? goal.getTheme().getName() : null)
                .areaId(goal.getArea() != null ? goal.getArea().getId() : null)
                .areaName(goal.getArea() != null ? goal.getArea().getName() : null)
                .title(goal.getTitle())
                .description(goal.getDescription())
                .sortOrder(goal.getSortOrder())
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .objectives(objectives)
                .build();
    }

    private ObjectiveResponse buildObjectiveResponse(Objective objective) {
        List<Long> univObjIds = objectiveMappingRepository
                .findByDeptObjectiveId(objective.getId())
                .stream().map(om -> om.getUniversityObjective().getId()).toList();

        List<InitiativeResponse> initiatives = initiativeRepository
                .findByObjectiveIdOrderBySortOrder(objective.getId())
                .stream().map(this::buildInitiativeResponse).toList();

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
                .initiatives(initiatives)
                .build();
    }

    private InitiativeResponse buildInitiativeResponse(Initiative initiative) {
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
}
