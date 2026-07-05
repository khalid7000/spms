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
import com.rit.spms.exception.StrategyIncompleteException;
import com.rit.spms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    private final AchievementRepository achievementRepository;
    private final AssessmentPeriodRepository assessmentPeriodRepository;
    private final PermissionService permissionService;
    private final AuditService auditService;
    private final ApprovalService approvalService;

    public Strategy createUniversityStrategy(CreateStrategyRequest req, Long currentUserId) {
        PlanningCycle cycle = planningCycleRepository.findById(req.getPlanningCycleId())
                .orElseThrow(() -> new ResourceNotFoundException("PlanningCycle", req.getPlanningCycleId()));

        if (strategyRepository.findByPlanningCycleIdAndDepartmentIsNullAndStrategyType(
                req.getPlanningCycleId(), StrategyType.UNIVERSITY).isPresent()) {
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
    public StrategyResponse getStrategy(Long strategyId, Long currentUserId, Long academicYearId) {
        permissionService.assertCanRead(currentUserId, strategyId);
        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy", strategyId));
        return buildStrategyResponse(strategy, true, academicYearId);
    }

    public Strategy changeState(Long strategyId, ChangeStateRequest req, Long currentUserId) {
        permissionService.assertOwner(currentUserId, strategyId);
        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy", strategyId));

        validateStateTransition(strategy.getState(), req.getNewState());

        if (strategy.getState() == StrategyState.CREATION && req.getNewState() == StrategyState.REVIEW) {
            validateCompleteness(strategyId);
        }

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

    private void validateCompleteness(Long strategyId) {
        List<VisionArea> areas = visionAreaRepository.findByStrategyIdOrderBySortOrder(strategyId);
        List<Long> areasWithoutGoals = new ArrayList<>();
        for (VisionArea area : areas) {
            if (!goalRepository.existsByStrategyIdAndAreaId(strategyId, area.getId())) {
                areasWithoutGoals.add(area.getId());
            }
        }

        List<Goal> goals = goalRepository.findByStrategyIdOrderBySortOrder(strategyId);
        List<Long> goalsWithoutObjectives = new ArrayList<>();
        List<Long> objectivesWithoutInitiatives = new ArrayList<>();
        List<Long> initiativesWithoutMeasurements = new ArrayList<>();

        for (Goal goal : goals) {
            if (!objectiveRepository.existsByGoalId(goal.getId())) {
                goalsWithoutObjectives.add(goal.getId());
                continue;
            }
            List<Objective> objectives = objectiveRepository.findByGoalIdOrderBySortOrder(goal.getId());
            for (Objective obj : objectives) {
                if (!initiativeRepository.existsByObjectiveIdAndAcademicYearIsNull(obj.getId())) {
                    objectivesWithoutInitiatives.add(obj.getId());
                    continue;
                }
                List<Initiative> initiatives = initiativeRepository
                        .findByObjectiveIdAndAcademicYearIsNullOrderBySortOrder(obj.getId());
                for (Initiative ini : initiatives) {
                    if (!measurementRepository.existsByInitiativeId(ini.getId())) {
                        initiativesWithoutMeasurements.add(ini.getId());
                    }
                }
            }
        }

        if (!areasWithoutGoals.isEmpty() || !goalsWithoutObjectives.isEmpty()
                || !objectivesWithoutInitiatives.isEmpty() || !initiativesWithoutMeasurements.isEmpty()) {
            throw new StrategyIncompleteException(
                    areasWithoutGoals, goalsWithoutObjectives, objectivesWithoutInitiatives, initiativesWithoutMeasurements);
        }
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
    public List<RoleAssignmentResponse> getMembers(Long strategyId, Long currentUserId) {
        permissionService.assertOwner(currentUserId, strategyId);
        return roleAssignmentRepository.findByStrategyId(strategyId)
                .stream().map(this::toRoleAssignmentResponse).toList();
    }

    public RoleAssignmentResponse assignMemberRole(Long strategyId, RoleAssignmentRequest req, Long currentUserId) {
        permissionService.assertOwner(currentUserId, strategyId);
        return toRoleAssignmentResponse(assignRole(strategyId, req, currentUserId));
    }

    public void revokeRole(Long strategyId, Long targetUserId, Long currentUserId) {
        permissionService.assertOwner(currentUserId, strategyId);
        if (currentUserId.equals(targetUserId)) {
            throw new BusinessRuleException("You cannot revoke your own access");
        }
        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy", strategyId));
        AppUser currentUser = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));
        RoleAssignment ra = roleAssignmentRepository.findByUserIdAndStrategyId(targetUserId, strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("RoleAssignment", targetUserId));
        if (ra.getRole() == RoleType.OWNER) {
            throw new BusinessRuleException("Cannot revoke the Owner role. Transfer ownership first.");
        }
        roleAssignmentRepository.delete(ra);
        auditService.log(currentUser, "REVOKE_ROLE", "RoleAssignment", ra.getId(), strategy,
                "Revoked access for user " + ra.getUser().getEmail());
    }

    private RoleAssignmentResponse toRoleAssignmentResponse(RoleAssignment ra) {
        AppUser u = ra.getUser();
        Strategy s = ra.getStrategy();
        return RoleAssignmentResponse.builder()
                .id(ra.getId())
                .strategyId(s.getId())
                .strategyTitle(s.getTitle())
                .userId(u.getId())
                .userEmail(u.getEmail())
                .userName(u.getFname() + " " + u.getLname())
                .role(ra.getRole())
                .build();
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
        return buildStrategyResponse(strategy, includeChildren, null);
    }

    public StrategyResponse buildStrategyResponse(Strategy strategy, boolean includeChildren, Long academicYearId) {
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

        boolean hasGoals = goalRepository.existsByStrategyId(strategy.getId());
        List<GoalResponse> goals = null;
        if (includeChildren) {
            goals = goalRepository.findByStrategyIdOrderBySortOrder(strategy.getId())
                    .stream().map(g -> buildGoalResponse(g, academicYearId)).toList();
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
                .hasGoals(hasGoals)
                .areas(areas)
                .goals(goals)
                .assessmentPeriods(assessmentPeriodRepository
                        .findByPlanningCycleIdOrderBySortOrder(strategy.getPlanningCycle().getId())
                        .stream().map(AssessmentPeriodResponse::from).toList())
                .build();
    }

    private GoalResponse buildGoalResponse(com.rit.spms.domain.Goal goal, Long academicYearId) {
        List<ObjectiveResponse> objectives = objectiveRepository
                .findByGoalIdOrderBySortOrder(goal.getId())
                .stream().map(obj -> buildObjectiveResponse(obj, academicYearId)).toList();
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

    private ObjectiveResponse buildObjectiveResponse(Objective objective, Long academicYearId) {
        List<Objective> univObjs = objectiveMappingRepository
                .findByDeptObjectiveId(objective.getId())
                .stream().map(om -> om.getUniversityObjective()).toList();
        List<Long> univObjIds = univObjs.stream().map(Objective::getId).toList();
        List<String> univObjTitles = univObjs.stream().map(Objective::getTitle).toList();

        // University-strategy initiatives are structural (academic_year_id IS NULL).
        // Never filter them by academic year — the year selector only affects dept breakdowns.
        boolean isUniversityObj = objective.getGoal().getStrategy().getStrategyType() == StrategyType.UNIVERSITY;
        List<Initiative> rawInitiatives = (academicYearId != null && !isUniversityObj)
                ? initiativeRepository.findByObjectiveIdAndAcademicYearIdOrderBySortOrder(objective.getId(), academicYearId)
                : initiativeRepository.findByObjectiveIdAndAcademicYearIsNullOrderBySortOrder(objective.getId());

        List<InitiativeResponse> initiatives = rawInitiatives
                .stream().map(this::buildInitiativeResponse).toList();

        return ObjectiveResponse.builder()
                .id(objective.getId())
                .goalId(objective.getGoal().getId())
                .title(objective.getTitle())
                .description(objective.getDescription())
                .sortOrder(objective.getSortOrder())
                .frozen(objective.getFrozen())
                .universityObjectiveIds(univObjIds)
                .universityObjectiveTitles(univObjTitles)
                .createdAt(objective.getCreatedAt())
                .updatedAt(objective.getUpdatedAt())
                .initiatives(initiatives)
                .build();
    }

    private InitiativeResponse buildInitiativeResponse(Initiative initiative) {
        Initiative univInit = initiativeMappingRepository.findByDeptInitiativeId(initiative.getId())
                .map(im -> im.getUniversityInitiative())
                .orElse(null);
        Long univInitId = univInit != null ? univInit.getId() : null;
        String univInitTitle = univInit != null ? univInit.getTitle() : null;

        long achievementCount = achievementRepository.countByMeasurementInitiativeId(initiative.getId());

        boolean isUniversity = initiative.getObjective().getGoal().getStrategy().getStrategyType() == StrategyType.UNIVERSITY;
        long mappedAchievementCount = 0;
        List<DepartmentAchievementSummary> deptBreakdown = null;
        if (isUniversity) {
            List<Object[]> rows = achievementRepository.countByPeriodAndDepartmentForUniversityInitiative(initiative.getId());
            if (!rows.isEmpty()) {
                deptBreakdown = rows.stream()
                        .map(r -> DepartmentAchievementSummary.builder()
                                .assessmentPeriodName((String) r[0])
                                .departmentName((String) r[1])
                                .achievementCount((Long) r[2])
                                .build())
                        .toList();
                mappedAchievementCount = deptBreakdown.stream()
                        .mapToLong(DepartmentAchievementSummary::getAchievementCount).sum();
            }
        }

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
                        .academicYearId(m.getAcademicYear() != null ? m.getAcademicYear().getId() : null)
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
                .universityInitiativeTitle(univInitTitle)
                .academicYearId(initiative.getAcademicYear() != null ? initiative.getAcademicYear().getId() : null)
                .hasAchievements(achievementCount > 0)
                .achievementCount(achievementCount)
                .mappedAchievementCount(mappedAchievementCount)
                .departmentBreakdown(deptBreakdown)
                .createdAt(initiative.getCreatedAt())
                .updatedAt(initiative.getUpdatedAt())
                .measurements(measurements)
                .build();
    }
}
