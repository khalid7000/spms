package com.rit.spms.service;

import com.rit.spms.domain.*;
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
    private final PasswordEncoder passwordEncoder;

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

    public UserResponse createUser(String fname, String lname, String email, String title,
                                   Long departmentId, boolean isAdmin, String password) {
        if (appUserRepository.existsByEmail(email)) {
            throw new BusinessRuleException("User with email '" + email + "' already exists");
        }
        Department dept = resolveOptionalDepartment(departmentId);
        String hash = password != null ? passwordEncoder.encode(password) : passwordEncoder.encode("changeme");
        AppUser user = AppUser.builder()
                .fname(fname).lname(lname).email(email).title(title)
                .department(dept).isAdmin(isAdmin).active(true).passwordHash(hash)
                .build();
        return UserResponse.from(appUserRepository.save(user));
    }

    public UserResponse updateUser(Long id, String fname, String lname, String title,
                                   Long departmentId, Boolean isAdmin) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", id));
        if (fname != null) user.setFname(fname);
        if (lname != null) user.setLname(lname);
        if (title != null) user.setTitle(title);
        if (isAdmin != null) user.setIsAdmin(isAdmin);
        if (departmentId != null) {
            user.setDepartment(resolveOptionalDepartment(departmentId));
        } else {
            user.setDepartment(null);
        }
        return UserResponse.from(appUserRepository.save(user));
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

    public PlanningCycle createPlanningCycle(String name, int startYear, int endYear) {
        return planningCycleRepository.save(
                PlanningCycle.builder().name(name).startYear(startYear).endYear(endYear).active(false).build());
    }

    public PlanningCycle updatePlanningCycle(Long id, String name, int startYear, int endYear, boolean active) {
        PlanningCycle cycle = planningCycleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PlanningCycle", id));
        cycle.setName(name);
        cycle.setStartYear(startYear);
        cycle.setEndYear(endYear);
        cycle.setActive(active);
        return planningCycleRepository.save(cycle);
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
        type.setName(name);
        type.setActive(active);
        return achievementTypeRepository.save(type);
    }

    public void deleteAchievementType(Long id) {
        AchievementType type = achievementTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AchievementType", id));
        type.setActive(false);
        achievementTypeRepository.save(type);
    }

    @Transactional(readOnly = true)
    public List<AchievementType> getAllAchievementTypes() {
        return achievementTypeRepository.findAll();
    }

    // --- Audit Logs ---

    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogs(Long strategyId, Pageable pageable) {
        if (strategyId != null) {
            return auditLogRepository.findByStrategyIdOrderByCreatedAtDesc(strategyId, pageable);
        }
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    // --- Strategies (admin view, no permission filter) ---

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
        return strategyRepository.save(strategy);
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
}
