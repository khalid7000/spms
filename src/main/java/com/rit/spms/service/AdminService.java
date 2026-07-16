package com.rit.spms.service;

import com.rit.spms.domain.*;
import com.rit.spms.domain.enums.RoleType;
import com.rit.spms.domain.enums.StrategyState;
import com.rit.spms.domain.enums.StrategyType;
import com.rit.spms.domain.enums.SystemRole;
import com.rit.spms.dto.response.AuditLogResponse;
import com.rit.spms.dto.response.UserResponse;
import com.rit.spms.exception.BusinessRuleException;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/** Admin-console CRUD: users (incl. system roles), org groups, departments, planning cycles, achievement types, audit log queries. */
@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final DepartmentRepository departmentRepository;
    private final AppUserRepository appUserRepository;
    private final PlanningCycleRepository planningCycleRepository;
    private final AssessmentPeriodRepository assessmentPeriodRepository;
    private final AchievementTypeRepository achievementTypeRepository;
    private final AuditLogRepository auditLogRepository;
    private final StrategyRepository strategyRepository;
    private final RoleAssignmentRepository roleAssignmentRepository;
    private final OrgGroupRepository orgGroupRepository;
    private final GoalRepository goalRepository;
    private final ThemeRepository themeRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final AcademicYearService academicYearService;
    private final EmployeeTitleRepository employeeTitleRepository;

    // --- Org Groups ---

    public OrgGroup createOrgGroup(String title, String headTitle, Long parentId, Long headUserId) {
        OrgGroup group = OrgGroup.builder()
                .title(title)
                .headTitle(headTitle)
                .parent(parentId != null
                        ? orgGroupRepository.findById(parentId)
                                .orElseThrow(() -> new ResourceNotFoundException("OrgGroup", parentId))
                        : null)
                .head(headUserId != null
                        ? appUserRepository.findById(headUserId)
                                .orElseThrow(() -> new ResourceNotFoundException("AppUser", headUserId))
                        : null)
                .build();
        return orgGroupRepository.save(group);
    }

    public OrgGroup updateOrgGroup(Long id, String title, String headTitle,
                                   Long parentId, Long headUserId) {
        OrgGroup group = orgGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OrgGroup", id));
        if (title != null) group.setTitle(title);
        if (headTitle != null) group.setHeadTitle(headTitle);
        group.setParent(parentId != null
                ? orgGroupRepository.findById(parentId)
                        .orElseThrow(() -> new ResourceNotFoundException("OrgGroup", parentId))
                : null);
        group.setHead(headUserId != null
                ? appUserRepository.findById(headUserId)
                        .orElseThrow(() -> new ResourceNotFoundException("AppUser", headUserId))
                : null);
        return orgGroupRepository.save(group);
    }

    public void deleteOrgGroup(Long id) {
        if (!orgGroupRepository.existsById(id)) {
            throw new ResourceNotFoundException("OrgGroup", id);
        }
        orgGroupRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<OrgGroup> getAllOrgGroups() {
        return orgGroupRepository.findAll();
    }

    // --- Departments ---

    public Department createDepartment(String name, String code, String headTitle,
                                        Long headUserId, Long orgGroupId) {
        if (departmentRepository.existsByCode(code)) {
            throw new BusinessRuleException("Department with code '" + code + "' already exists");
        }
        return departmentRepository.save(Department.builder()
                .name(name).code(code).active(true).headTitle(headTitle)
                .head(headUserId != null
                        ? appUserRepository.findById(headUserId)
                                .orElseThrow(() -> new ResourceNotFoundException("AppUser", headUserId))
                        : null)
                .orgGroup(orgGroupId != null
                        ? orgGroupRepository.findById(orgGroupId)
                                .orElseThrow(() -> new ResourceNotFoundException("OrgGroup", orgGroupId))
                        : null)
                .build());
    }

    public Department updateDepartment(Long id, String name, String code,
                                       String headTitle, Long headUserId, Long orgGroupId) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
        dept.setName(name);
        if (!dept.getCode().equals(code)) {
            if (departmentRepository.existsByCode(code)) {
                throw new BusinessRuleException("Department with code '" + code + "' already exists");
            }
            dept.setCode(code);
        }
        dept.setHeadTitle(headTitle);
        dept.setHead(headUserId != null
                ? appUserRepository.findById(headUserId)
                        .orElseThrow(() -> new ResourceNotFoundException("AppUser", headUserId))
                : null);
        dept.setOrgGroup(orgGroupId != null
                ? orgGroupRepository.findById(orgGroupId)
                        .orElseThrow(() -> new ResourceNotFoundException("OrgGroup", orgGroupId))
                : null);
        return departmentRepository.save(dept);
    }

    public void deactivateDepartment(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
        dept.setActive(false);
        departmentRepository.save(dept);
    }

    public void reactivateDepartment(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
        dept.setActive(true);
        departmentRepository.save(dept);
    }

    @Transactional(readOnly = true)
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Department getDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
    }

    // --- Users ---

    /**
     * {@code currentUserId} is the caller, resolved and checked for full ADMIN before {@code
     * systemRoles} is ever applied -- a USER_ADMIN caller (limited to user management, granted by
     * a true ADMIN) must never be able to grant ADMIN/HR/USER_ADMIN to anyone, including via a
     * crafted request that bypasses the UI, which only shows the roles field to a full ADMIN in
     * the first place. This check is the actual security boundary, not the UI.
     */
    public UserResponse createUser(String fname, String lname, String email, String title,
                                   Long departmentId, Long orgGroupId, Set<SystemRole> systemRoles,
                                   String password, Long currentUserId) {
        if (appUserRepository.existsByEmail(email)) {
            throw new BusinessRuleException("User with email '" + email + "' already exists");
        }
        assertKnownTitleIfUserAdmin(title, currentUserId);
        Department dept = resolveOptionalDepartment(departmentId);
        OrgGroup group = resolveOptionalOrgGroup(orgGroupId);
        // Self-registration (AuthController.register, currentUserId == null) is exempt -- it has no
        // frontend page and always creates users with neither, and there's nowhere for it to ask.
        // Every admin-driven call (currentUserId != null) must supply at least one.
        if (currentUserId != null && dept == null && group == null) {
            throw new BusinessRuleException("User must be assigned to either a department or an org group");
        }
        String hash = password != null ? passwordEncoder.encode(password) : passwordEncoder.encode("changeme");
        Set<SystemRole> effectiveRoles = callerIsFullAdmin(currentUserId) && systemRoles != null ? systemRoles : Set.of();
        AppUser user = AppUser.builder()
                .fname(fname).lname(lname).email(email).title(title)
                .department(dept).orgGroup(group).systemRoles(effectiveRoles).active(true).passwordHash(hash)
                .build();
        return UserResponse.from(appUserRepository.save(user));
    }

    public UserResponse updateUser(Long id, String fname, String lname, String title,
                                   Long departmentId, Long orgGroupId, Set<SystemRole> systemRoles,
                                   Long currentUserId) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", id));
        if (fname != null) user.setFname(fname);
        if (lname != null) user.setLname(lname);
        if (title != null) {
            assertKnownTitleIfUserAdmin(title, currentUserId);
            user.setTitle(title);
        }
        if (systemRoles != null && callerIsFullAdmin(currentUserId)) user.setSystemRoles(systemRoles);
        if (departmentId != null) {
            user.setDepartment(resolveOptionalDepartment(departmentId));
        } else {
            user.setDepartment(null);
        }
        if (orgGroupId != null) {
            user.setOrgGroup(resolveOptionalOrgGroup(orgGroupId));
        } else {
            user.setOrgGroup(null);
        }
        if (user.getDepartment() == null && user.getOrgGroup() == null) {
            throw new BusinessRuleException("User must be assigned to either a department or an org group");
        }
        return UserResponse.from(appUserRepository.save(user));
    }

    /**
     * A USER_ADMIN (authenticated, but not a full ADMIN) can only assign a title that already
     * exists in {@link com.rit.spms.domain.EmployeeTitle} -- they can edit/add users but must not
     * be able to introduce a brand-new title string. Full ADMINs are unrestricted (unchanged
     * behavior), and so is the public self-registration flow (currentUserId is null there) --
     * this check only fires for an authenticated non-admin caller, i.e. specifically USER_ADMIN.
     */
    private void assertKnownTitleIfUserAdmin(String title, Long currentUserId) {
        if (title == null || title.isBlank() || currentUserId == null || callerIsFullAdmin(currentUserId)) {
            return;
        }
        if (employeeTitleRepository.findByTitleNameIgnoreCase(title.trim()).isEmpty()) {
            throw new BusinessRuleException("Unknown title '" + title + "' -- User Admins can only assign an existing title");
        }
    }

    // currentUserId is null for the public self-registration endpoint (AuthController.register) --
    // never a full admin there, which is already what it wants (always Set.of() for self-signups).
    private boolean callerIsFullAdmin(Long currentUserId) {
        if (currentUserId == null) {
            return false;
        }
        return appUserRepository.findById(currentUserId)
                .map(u -> u.hasRole(SystemRole.ADMIN))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return appUserRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AppUser getUser(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", id));
    }

    // --- Planning Cycles ---

    public PlanningCycle createPlanningCycle(String name, int startYear, int endYear,
                                              Long ownerId, Long adminUserId) {
        PlanningCycle cycle = planningCycleRepository.save(
                PlanningCycle.builder().name(name).startYear(startYear).endYear(endYear).active(false).build());

        String strategyTitle = name + " Strategic Plan";
        String strategyDescription = "Main university strategic plan for the " + name
                + " planning cycle (" + startYear + "–" + endYear + ").";
        createUniversityStrategy(cycle.getId(), strategyTitle, strategyDescription,
                ownerId, StrategyType.UNIVERSITY, adminUserId);

        return cycle;
    }

    public void updatePlanningCycle(Long id, String name, int startYear, int endYear, boolean active) {
        PlanningCycle cycle = planningCycleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PlanningCycle", id));
        cycle.setName(name);
        cycle.setStartYear(startYear);
        cycle.setEndYear(endYear);
        cycle.setActive(active);
        planningCycleRepository.save(cycle);
    }

    public void deletePlanningCycle(Long id) {
        PlanningCycle cycle = planningCycleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PlanningCycle", id));
        if (strategyRepository.existsByPlanningCycleId(id)) {
            throw new BusinessRuleException("Cannot delete a planning cycle that has strategies. Delete all strategies first.");
        }
        themeRepository.deleteByPlanningCycleId(id);
        assessmentPeriodRepository.deleteByPlanningCycleId(id);
        planningCycleRepository.delete(cycle);
    }

    @Transactional(readOnly = true)
    public List<PlanningCycle> getAllPlanningCycles() {
        return planningCycleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public PlanningCycle getPlanningCycle(Long id) {
        return planningCycleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PlanningCycle", id));
    }

    // --- Assessment Periods ---

    public AssessmentPeriod createAssessmentPeriod(Long cycleId, String name,
                                                    LocalDate startDate, LocalDate endDate, int sortOrder) {
        PlanningCycle cycle = planningCycleRepository.findById(cycleId)
                .orElseThrow(() -> new ResourceNotFoundException("PlanningCycle", cycleId));
        return assessmentPeriodRepository.save(
                AssessmentPeriod.builder()
                        .planningCycle(cycle).name(name)
                        .startDate(startDate).endDate(endDate).sortOrder(sortOrder)
                        .build());
    }

    public void deleteAssessmentPeriod(Long id) {
        AssessmentPeriod period = assessmentPeriodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AssessmentPeriod", id));
        assessmentPeriodRepository.delete(period);
    }

    @Transactional(readOnly = true)
    public List<AssessmentPeriod> getPeriodsForCycle(Long cycleId) {
        return assessmentPeriodRepository.findByPlanningCycleIdOrderBySortOrder(cycleId);
    }

    // --- Achievement Types ---

    public AchievementType createAchievementType(String name) {
        if (achievementTypeRepository.existsByName(name)) {
            throw new BusinessRuleException("Achievement type '" + name + "' already exists");
        }
        return achievementTypeRepository.save(AchievementType.builder().name(name).active(true).build());
    }

    public AchievementType updateAchievementType(Long id, String name, boolean active) {
        AchievementType type = achievementTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AchievementType", id));
        // System-linked rows (see AchievementType.systemCode) power other code paths -- e.g. the
        // "Other" custom-type-name flow and the Teaching Evaluations module's gating -- and can be
        // freely renamed, but never deactivated, through either this or deleteAchievementType.
        if (!active && type.getSystemCode() != null) {
            throw new BusinessRuleException("Cannot deactivate a system-linked achievement type ('" + type.getName() + "')");
        }
        type.setName(name);
        type.setActive(active);
        return achievementTypeRepository.save(type);
    }

    public void deleteAchievementType(Long id) {
        AchievementType type = achievementTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AchievementType", id));
        if (type.getSystemCode() != null) {
            throw new BusinessRuleException("Cannot deactivate a system-linked achievement type ('" + type.getName() + "')");
        }
        type.setActive(false);
        achievementTypeRepository.save(type);
    }

    @Transactional(readOnly = true)
    public List<AchievementType> getAllAchievementTypes() {
        return achievementTypeRepository.findAll();
    }

    // --- Audit Logs ---

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogs(Long strategyId, Pageable pageable) {
        if (strategyId != null) {
            return auditLogRepository.findByStrategyIdOrderByCreatedAtDesc(strategyId, pageable)
                    .map(AuditLogResponse::from);
        }
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(AuditLogResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getStrategyAuditLog(Long strategyId, Pageable pageable) {
        return auditLogRepository.findByStrategyIdOrderByCreatedAtDesc(strategyId, pageable)
                .map(AuditLogResponse::from);
    }

    // --- Strategies (admin view, no permission filter) ---

    public Strategy createUniversityStrategy(Long planningCycleId, String title, String description,
                                              Long ownerId, StrategyType type, Long adminUserId) {
        PlanningCycle cycle = planningCycleRepository.findById(planningCycleId)
                .orElseThrow(() -> new ResourceNotFoundException("PlanningCycle", planningCycleId));

        if (type == StrategyType.UNIVERSITY &&
                strategyRepository.findByPlanningCycleIdAndDepartmentIsNullAndStrategyType(
                        planningCycleId, StrategyType.UNIVERSITY).isPresent()) {
            throw new BusinessRuleException("A main university strategy already exists for this planning cycle");
        }

        Strategy strategy = Strategy.builder()
                .planningCycle(cycle)
                .strategyType(type)
                .state(StrategyState.CREATION)
                .title(title)
                .description(description)
                .build();
        strategy = strategyRepository.save(strategy);

        AppUser owner = appUserRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", ownerId));
        roleAssignmentRepository.save(RoleAssignment.builder()
                .user(owner).strategy(strategy).role(RoleType.OWNER).build());

        AppUser admin = appUserRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", adminUserId));
        auditService.log(admin, "CREATE_STRATEGY", "Strategy", strategy.getId(), strategy,
                "Admin created university strategy: " + title + " — assigned owner: " + owner.getEmail());
        return strategy;
    }

    public void deleteStrategy(Long id) {
        Strategy strategy = strategyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy", id));
        if (goalRepository.existsByStrategyId(id)) {
            throw new BusinessRuleException("Cannot delete a strategy that has goals. Remove all goals first.");
        }
        auditLogRepository.clearStrategyReferences(id);
        roleAssignmentRepository.deleteByStrategyId(id);
        strategyRepository.delete(strategy);
    }

    @Transactional(readOnly = true)
    public List<Strategy> getAllStrategies() {
        return strategyRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Strategy getStrategyById(Long strategyId) {
        return strategyRepository.findById(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy", strategyId));
    }

    public Strategy adminOverrideState(Long strategyId, com.rit.spms.domain.enums.StrategyState newState) {
        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy", strategyId));
        strategy.setState(newState);
        Strategy saved = strategyRepository.save(strategy);
        if (newState == com.rit.spms.domain.enums.StrategyState.DEPLOYED) {
            academicYearService.backfillInitiativeCopiesForNewlyDeployedStrategy(saved);
        }
        return saved;
    }

    // --- Role Assignments ---

    @Transactional(readOnly = true)
    public List<RoleAssignment> getStrategyAssignments(Long strategyId) {
        strategyRepository.findById(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy", strategyId));
        return roleAssignmentRepository.findByStrategyId(strategyId);
    }

    @Transactional(readOnly = true)
    public List<RoleAssignment> getUserAssignments(Long userId) {
        appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", userId));
        return roleAssignmentRepository.findByUserId(userId);
    }

    public void deleteAssignment(Long assignmentId) {
        RoleAssignment assignment = roleAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("RoleAssignment", assignmentId));
        roleAssignmentRepository.delete(assignment);
    }

    // --- Helpers ---

    private Department resolveOptionalDepartment(Long departmentId) {
        if (departmentId == null) return null;
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", departmentId));
    }

    private OrgGroup resolveOptionalOrgGroup(Long orgGroupId) {
        if (orgGroupId == null) return null;
        return orgGroupRepository.findById(orgGroupId)
                .orElseThrow(() -> new ResourceNotFoundException("OrgGroup", orgGroupId));
    }
}
