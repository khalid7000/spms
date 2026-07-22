package com.rit.spms.service;

import com.rit.spms.domain.AnnualEvaluation;
import com.rit.spms.domain.AppUser;
import com.rit.spms.domain.ApprovalDelegation;
import com.rit.spms.domain.Department;
import com.rit.spms.domain.Strategy;
import com.rit.spms.domain.SwotParticipant;
import com.rit.spms.domain.SwotSession;
import com.rit.spms.domain.OrgGroup;
import com.rit.spms.domain.VsmAuthorGrant;
import com.rit.spms.domain.VsmMap;
import com.rit.spms.domain.enums.ApprovalDelegationStatus;
import com.rit.spms.domain.enums.RoleType;
import com.rit.spms.domain.enums.StrategyState;
import com.rit.spms.domain.enums.SwotPhase;
import com.rit.spms.domain.enums.SystemRole;
import com.rit.spms.domain.enums.VsmAuthorGrantStatus;
import com.rit.spms.domain.enums.VsmScopeType;
import com.rit.spms.exception.BusinessRuleException;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.exception.UnauthorizedException;
import com.rit.spms.repository.AppUserRepository;
import com.rit.spms.repository.ApprovalDelegationRepository;
import com.rit.spms.repository.DepartmentRepository;
import com.rit.spms.repository.OrgGroupRepository;
import com.rit.spms.repository.RoleAssignmentRepository;
import com.rit.spms.repository.StrategyApprovalRepository;
import com.rit.spms.repository.StrategyRepository;
import com.rit.spms.repository.SwotParticipantRepository;
import com.rit.spms.repository.SwotSessionRepository;
import com.rit.spms.repository.VisionAreaRepository;
import com.rit.spms.repository.VsmAuthorGrantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Central authorization checks for Strategy roles/state, SWOT phases, Portfolio goal-cycle
 * management, and Annual Evaluation report access -- one place for the "who is allowed to do X"
 * questions the rest of the app's services delegate to rather than re-deriving inline.
 */
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final RoleAssignmentRepository roleAssignmentRepository;
    private final StrategyRepository strategyRepository;
    private final StrategyApprovalRepository strategyApprovalRepository;
    private final SwotSessionRepository swotSessionRepository;
    private final SwotParticipantRepository swotParticipantRepository;
    private final VisionAreaRepository visionAreaRepository;
    private final AppUserRepository appUserRepository;
    private final DepartmentRepository departmentRepository;
    private final OrgGroupRepository orgGroupRepository;
    private final VsmAuthorGrantRepository vsmAuthorGrantRepository;
    private final ApprovalDelegationRepository approvalDelegationRepository;

    // ─── Employee Portfolio & Goals ────────────────────────────────────────

    /**
     * The user who supervises `employee` for goal-setting/evaluation purposes: their department
     * head, or -- if the employee IS their own department's head, which would otherwise resolve
     * to themselves -- the head of the nearest ancestor org group (walking up the parent chain,
     * since a department head's own supervisor isn't tracked at the department level). Empty if
     * neither can be resolved (no department head at all, or no org group head above a self-heading
     * department head).
     */
    public Optional<AppUser> resolveSupervisor(AppUser employee) {
        Department dept = employee.getDepartment();
        if (dept == null || dept.getHead() == null) {
            return Optional.empty();
        }
        if (!dept.getHead().getId().equals(employee.getId())) {
            return Optional.of(dept.getHead());
        }
        OrgGroup group = dept.getOrgGroup();
        while (group != null) {
            if (group.getHead() != null && !group.getHead().getId().equals(employee.getId())) {
                return Optional.of(group.getHead());
            }
            group = group.getParent();
        }
        return Optional.empty();
    }

    /**
     * Resolves the employee's supervisor (see {@link #resolveSupervisor}) and asserts the current
     * user is either that supervisor or an admin. The supervisor is derived server-side (never
     * client-supplied) so a client can't set arbitrary goals for an employee they don't actually lead.
     */
    public AppUser resolveAndAssertCanManageGoalsFor(Long currentUserId, AppUser employee) {
        AppUser leader = resolveSupervisor(employee)
                .orElseThrow(() -> new BusinessRuleException(
                        "Employee has no supervisor assigned; cannot set goals for this employee"));
        AppUser currentUser = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));
        boolean isLeader = leader.getId().equals(currentUserId);
        boolean isAdmin = currentUser.hasRole(SystemRole.ADMIN);
        if (!isLeader && !isAdmin) {
            throw new UnauthorizedException("Only this employee's supervisor or an admin can manage their goals");
        }
        return leader;
    }

    /** Self, the employee's resolved department head, or an admin may view an employee's portfolio. */
    public void assertCanViewPortfolioOf(Long currentUserId, AppUser employee) {
        if (employee.getId().equals(currentUserId)) {
            return;
        }
        AppUser currentUser = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));
        if (currentUser.hasRole(SystemRole.ADMIN)) {
            return;
        }
        Department dept = employee.getDepartment();
        boolean isHead = dept != null && dept.getHead() != null && dept.getHead().getId().equals(currentUserId);
        if (!isHead) {
            throw new UnauthorizedException("Only the employee, their department head, or an admin can view this portfolio");
        }
    }

    /**
     * Walks the same Department.head -> OrgGroup.parent chain ApprovalService uses to build
     * approval chains, but to answer a permission question instead: does `candidate` sit anywhere
     * above `employee` in the reporting hierarchy (Faculty -> Chair -> Dean -> Provost, etc.)?
     */
    public boolean isAboveInHierarchy(AppUser candidate, AppUser employee) {
        Department dept = employee.getDepartment();
        if (dept == null) {
            return false;
        }
        AppUser deptHead = dept.getHead();
        if (deptHead != null && deptHead.getId().equals(candidate.getId())) {
            return true;
        }
        OrgGroup group = dept.getOrgGroup();
        while (group != null) {
            AppUser groupHead = group.getHead();
            if (groupHead != null && groupHead.getId().equals(candidate.getId())) {
                return true;
            }
            group = group.getParent();
        }
        return false;
    }

    /**
     * Only the employee, HR/Admin, or anyone above the employee in the reporting hierarchy
     * (Faculty -> Chair -> Dean -> Provost) may pull a report of a concluded Annual Evaluation.
     */
    public void assertCanViewEvaluationReport(Long currentUserId, AnnualEvaluation evaluation) {
        if (evaluation.getEmployee().getId().equals(currentUserId)) {
            return;
        }
        AppUser currentUser = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));
        if (currentUser.hasRole(SystemRole.ADMIN) || currentUser.hasRole(SystemRole.HR)) {
            return;
        }
        if (isAboveInHierarchy(currentUser, evaluation.getEmployee())) {
            return;
        }
        throw new UnauthorizedException("Only the employee, HR, an admin, or someone above them in the reporting hierarchy can view this report");
    }

    /**
     * Only HR/Admin or anyone above the employee in the reporting hierarchy (Faculty -> Chair ->
     * Dean -> Provost) may use a Criteria Info Tool -- deliberately narrower than {@link
     * #assertCanViewEvaluationReport}: the whole point of these tools is reference info for the
     * EVALUATOR, so unlike that method, the employee themselves is never allowed here, even for
     * their own evaluation. This is the actual security boundary for that data; the frontend simply
     * not rendering the button is not sufficient on its own.
     */
    public void assertCanUseCriteriaInfoTool(Long currentUserId, AnnualEvaluation evaluation) {
        AppUser currentUser = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));
        if (currentUser.hasRole(SystemRole.ADMIN) || currentUser.hasRole(SystemRole.HR)) {
            return;
        }
        if (isAboveInHierarchy(currentUser, evaluation.getEmployee())) {
            return;
        }
        throw new UnauthorizedException("Only HR, an admin, or someone above this employee in the reporting hierarchy can use this tool");
    }

    /**
     * Every department the user heads directly, plus every department under any org group they
     * head (recursively through sub-groups) -- shared by Organization Evaluations' hierarchy
     * rollup and by the "is this console redundant with Team Evaluations" check (redundant exactly
     * when this set is no bigger than the departments the user heads directly -- see
     * UserController#getMyLeadership).
     */
    public Set<Long> resolveHierarchyDepartmentIds(Long userId) {
        Set<Long> departmentIds = new HashSet<>();
        for (Department dept : departmentRepository.findByHeadId(userId)) {
            departmentIds.add(dept.getId());
        }

        Deque<OrgGroup> queue = new ArrayDeque<>(orgGroupRepository.findByHeadId(userId));
        Set<Long> visitedGroupIds = new HashSet<>();
        while (!queue.isEmpty()) {
            OrgGroup group = queue.poll();
            if (!visitedGroupIds.add(group.getId())) {
                continue;
            }
            for (Department dept : departmentRepository.findByOrgGroupId(group.getId())) {
                departmentIds.add(dept.getId());
            }
            queue.addAll(orgGroupRepository.findByParentId(group.getId()));
        }
        return departmentIds;
    }

    /** Only that department's head, or an admin, may create a strategy for it. */
    public void assertCanCreateDepartmentStrategy(Long currentUserId, Long departmentId) {
        AppUser currentUser = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));
        if (currentUser.hasRole(SystemRole.ADMIN)) {
            return;
        }
        boolean isHead = departmentRepository.findByHeadId(currentUserId).stream()
                .anyMatch(d -> d.getId().equals(departmentId));
        if (!isHead) {
            throw new UnauthorizedException("Only this department's head or an admin can create a strategy for it");
        }
    }

    /** Only the root Org Group's head (e.g. the Provost), or an admin, may create the university strategy. */
    public void assertCanCreateUniversityStrategy(Long currentUserId) {
        AppUser currentUser = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));
        if (currentUser.hasRole(SystemRole.ADMIN)) {
            return;
        }
        boolean isRootHead = orgGroupRepository.findByParentIsNull().stream()
                .anyMatch(g -> g.getHead() != null && g.getHead().getId().equals(currentUserId));
        if (!isRootHead) {
            throw new UnauthorizedException("Only the head of the top-level Org Group or an admin can create the university strategy");
        }
    }

    // ─── Value Stream Mapping (Phase 1) ────────────────────────────────────

    /**
     * Only that unit's head, an admin, or an employee holding an ACTIVE {@link VsmAuthorGrant} for
     * this exact scope, may create a VSM map for it -- mirrors {@link
     * #assertCanCreateDepartmentStrategy}/{@link #assertCanCreateUniversityStrategy} generalized to
     * any org-group (not just the root).
     */
    public void assertCanCreateVsmMap(Long currentUserId, VsmScopeType scopeType, Long scopeId) {
        AppUser currentUser = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));
        if (currentUser.hasRole(SystemRole.ADMIN)) {
            return;
        }
        boolean isHead = scopeType == VsmScopeType.DEPARTMENT
                ? departmentRepository.findByHeadId(currentUserId).stream().anyMatch(d -> d.getId().equals(scopeId))
                : orgGroupRepository.findByHeadId(currentUserId).stream().anyMatch(g -> g.getId().equals(scopeId));
        if (isHead) {
            return;
        }
        boolean hasActiveGrant = scopeType == VsmScopeType.DEPARTMENT
                ? vsmAuthorGrantRepository.existsByEmployeeIdAndScopeTypeAndDepartmentIdAndStatus(
                        currentUserId, scopeType, scopeId, VsmAuthorGrantStatus.ACTIVE)
                : vsmAuthorGrantRepository.existsByEmployeeIdAndScopeTypeAndOrgGroupIdAndStatus(
                        currentUserId, scopeType, scopeId, VsmAuthorGrantStatus.ACTIVE);
        if (!hasActiveGrant) {
            throw new UnauthorizedException(
                    "Only this unit's head, an admin, or someone granted VSM author rights for it can create a Value Stream Map here");
        }
    }

    /** Walks up from a department to the root OrgGroup (no parent) -- extracted from {@code
     *  ApprovalService.buildTopOfHierarchyChain}'s walk so both that flow and VSM author-grant
     *  approval share one implementation. */
    public Optional<OrgGroup> resolveTopOfHierarchyGroup(Department department) {
        return resolveTopOfHierarchyGroup(department != null ? department.getOrgGroup() : null);
    }

    public Optional<OrgGroup> resolveTopOfHierarchyGroup(OrgGroup startGroup) {
        OrgGroup group = startGroup;
        OrgGroup root = null;
        while (group != null) {
            root = group;
            group = group.getParent();
        }
        return Optional.ofNullable(root);
    }

    /** The root OrgGroup's head, if any -- e.g. the Provost. */
    public Optional<AppUser> resolveTopOfHierarchyHead(Department department) {
        return resolveTopOfHierarchyGroup(department).map(OrgGroup::getHead);
    }

    public Optional<AppUser> resolveTopOfHierarchyHead(OrgGroup startGroup) {
        return resolveTopOfHierarchyGroup(startGroup).map(OrgGroup::getHead);
    }

    /**
     * Every nominal-approver resolution in the app (ApprovalService's chains, VSM author-grant
     * approval) routes through this one method, so an {@link ApprovalDelegation} -- set up through
     * the Approval Delegation Console -- transparently redirects the approval to the delegate for
     * as long as it's ACTIVE and {@code onDate} falls within its [startDate, endDate] window. Falls
     * back to {@code nominalApprover} unchanged when no such delegation exists. Deliberately a
     * single-hop lookup (does not chase the delegate's own delegations): re-delegation is blocked
     * structurally anyway, since creating a delegation requires actually being the department/org-
     * group's recorded head, which a delegate never becomes.
     */
    public AppUser resolveEffectiveApprover(AppUser nominalApprover, LocalDate onDate) {
        return approvalDelegationRepository
                .findFirstByDelegatorIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        nominalApprover.getId(), ApprovalDelegationStatus.ACTIVE, onDate, onDate)
                .map(ApprovalDelegation::getDelegate)
                .orElse(nominalApprover);
    }

    /** Only the map's author, or an admin, may edit its canvas (nodes/edges/metrics) or state. */
    public void assertCanEditVsmMap(Long currentUserId, VsmMap map) {
        if (!canEditVsmMap(currentUserId, map)) {
            throw new UnauthorizedException("Only this map's author or an admin can edit it");
        }
    }

    /** Non-throwing form of {@link #assertCanEditVsmMap} -- used where the caller wants to branch
     *  behavior (e.g. VsmBoardService including BACKLOG tasks) rather than reject the request. */
    public boolean canEditVsmMap(Long currentUserId, VsmMap map) {
        AppUser currentUser = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));
        return currentUser.hasRole(SystemRole.ADMIN) || map.getCreatedBy().getId().equals(currentUserId);
    }

    /**
     * Read access: the map's author, an admin, or -- for a department-scoped map -- anyone in that
     * same department. Org-group-scoped maps are author/admin-only for now; broadening that to
     * "anyone under this org group's hierarchy" is a later concern once a rollup board needs that
     * wider audience too.
     */
    public void assertCanViewVsmMap(Long currentUserId, VsmMap map) {
        AppUser currentUser = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));
        if (currentUser.hasRole(SystemRole.ADMIN) || map.getCreatedBy().getId().equals(currentUserId)) {
            return;
        }
        boolean sameDepartment = map.getScopeType() == VsmScopeType.DEPARTMENT
                && map.getDepartment() != null
                && currentUser.getDepartment() != null
                && currentUser.getDepartment().getId().equals(map.getDepartment().getId());
        if (!sameDepartment) {
            throw new UnauthorizedException("You do not have access to this Value Stream Map");
        }
    }

    /** Only that department's head, an admin, or anyone whose own department is this one, may view
     *  its rollup Kanban board (see VsmBoardService#getDepartmentBoard) -- the same audience that
     *  can browse and pull the improvement tasks it lists. */
    public void assertCanViewDepartmentBoard(Long currentUserId, Long departmentId) {
        AppUser currentUser = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));
        if (currentUser.hasRole(SystemRole.ADMIN)) {
            return;
        }
        boolean isHead = departmentRepository.findByHeadId(currentUserId).stream()
                .anyMatch(d -> d.getId().equals(departmentId));
        boolean isMember = currentUser.getDepartment() != null
                && currentUser.getDepartment().getId().equals(departmentId);
        if (!isHead && !isMember) {
            throw new UnauthorizedException("You do not have access to this department's task board");
        }
    }

    public RoleType getUserRole(Long userId, Long strategyId) {
        return roleAssignmentRepository.findByUserIdAndStrategyId(userId, strategyId)
                .map(ra -> ra.getRole())
                .orElse(null);
    }

    public boolean isOwner(Long userId, Long strategyId) {
        return RoleType.OWNER.equals(getUserRole(userId, strategyId));
    }

    public boolean canEdit(Long userId, Long strategyId) {
        RoleType role = getUserRole(userId, strategyId);
        return role == RoleType.OWNER || role == RoleType.EDITOR;
    }

    public boolean canComment(Long userId, Long strategyId) {
        RoleType role = getUserRole(userId, strategyId);
        if (role == null) return false;
        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy", strategyId));
        StrategyState state = strategy.getState();
        if (state == StrategyState.APPROVAL_PENDING) return false;
        if (state == StrategyState.FROZEN) return role == RoleType.OWNER;
        if (state == StrategyState.DEPLOYED)
            return role == RoleType.OWNER || role == RoleType.EDITOR || role == RoleType.COMMENTER;
        return role == RoleType.OWNER || role == RoleType.EDITOR || role == RoleType.COMMENTER;
    }

    /** Role assignment OR a pending approval record grants read access. */
    public boolean canRead(Long userId, Long strategyId) {
        if (getUserRole(userId, strategyId) != null) return true;
        return strategyApprovalRepository
                .existsByStrategyIdAndRequiredApproverIdAndApprovedFalse(strategyId, userId);
    }

    public boolean canAddInitiative(Long userId, Long strategyId) {
        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy", strategyId));
        StrategyState state = strategy.getState();
        if (state != StrategyState.CREATION) {
            return false;
        }
        return canEdit(userId, strategyId);
    }

    public void assertCanRead(Long userId, Long strategyId) {
        if (!canRead(userId, strategyId)) {
            throw new UnauthorizedException("You do not have access to this strategy");
        }
    }

    public void assertCanEdit(Long userId, Long strategyId) {
        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy", strategyId));

        if (!canEdit(userId, strategyId)) {
            throw new UnauthorizedException("You do not have edit access to this strategy");
        }

        StrategyState state = strategy.getState();
        if (state == StrategyState.FROZEN) {
            throw new UnauthorizedException("Strategy content cannot be edited in FROZEN state");
        }
        if (state == StrategyState.DEPLOYED) {
            throw new UnauthorizedException("Plan content cannot be edited in DEPLOYED state");
        }
        if (state == StrategyState.APPROVAL_PENDING) {
            throw new UnauthorizedException("Strategy is locked while awaiting deployment approval");
        }
    }

    public void assertCanEditContent(Long userId, Long strategyId) {
        assertCanEdit(userId, strategyId);
    }

    public void assertOwner(Long userId, Long strategyId) {
        if (!isOwner(userId, strategyId)) {
            throw new UnauthorizedException("Only the Owner can perform this action");
        }
    }

    public void assertCanComment(Long userId, Long strategyId) {
        if (!canComment(userId, strategyId)) {
            throw new UnauthorizedException("You do not have commenting access to this strategy");
        }
    }

    public void assertCanAddAchievement(Long userId, Long strategyId) {
        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy", strategyId));
        if (strategy.getState() != StrategyState.DEPLOYED) {
            throw new UnauthorizedException("Achievements can only be added when the strategy is DEPLOYED");
        }
        if (!canEdit(userId, strategyId)) {
            throw new UnauthorizedException("Only Owner or Editor can add achievements");
        }
    }

    // ─── SWOT collaborative workflow ────────────────────────────────────────

    private SwotSession requireSession(Long strategyId) {
        return swotSessionRepository.findByStrategyId(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("SwotSession for strategy", strategyId));
    }

    private SwotParticipant requireParticipant(SwotSession session, Long userId) {
        return swotParticipantRepository.findBySwotSessionIdAndUserId(session.getId(), userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a participant in this strategy's SWOT session"));
    }

    public void assertCanStartSwot(Long userId, Long strategyId) {
        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy", strategyId));
        assertOwner(userId, strategyId);
        if (strategy.getState() != StrategyState.CREATION) {
            throw new UnauthorizedException("SWOT analysis can only be started while the strategy is in CREATION");
        }
        if (swotSessionRepository.existsByStrategyId(strategyId)) {
            throw new UnauthorizedException("A SWOT session already exists for this strategy");
        }
        if (visionAreaRepository.existsByStrategyId(strategyId)) {
            throw new UnauthorizedException("SWOT analysis cannot be started once vision areas already exist");
        }
    }

    public void assertCanSubmitSwotEntry(Long userId, Long strategyId) {
        SwotSession session = requireSession(strategyId);
        SwotParticipant participant = requireParticipant(session, userId);
        if (session.getPhase() != SwotPhase.COLLECTING) {
            throw new UnauthorizedException("SWOT word collection is not open for this strategy");
        }
        if (participant.getSwotSubmittedAt() != null) {
            throw new UnauthorizedException("You have already submitted your SWOT analysis");
        }
    }

    public boolean canViewOwnVisualization(Long userId, Long strategyId) {
        Optional<SwotSession> session = swotSessionRepository.findByStrategyId(strategyId);
        if (session.isEmpty()) return false;
        return swotParticipantRepository.findBySwotSessionIdAndUserId(session.get().getId(), userId)
                .map(p -> p.getSwotSubmittedAt() != null)
                .orElse(false);
    }

    public void assertCanVote(Long userId, Long strategyId) {
        SwotSession session = requireSession(strategyId);
        SwotParticipant participant = requireParticipant(session, userId);
        if (session.getPhase() != SwotPhase.VOTING) {
            throw new UnauthorizedException("Voting is not open for this strategy");
        }
        if (participant.getVoteSubmittedAt() != null) {
            throw new UnauthorizedException("You have already submitted your vote");
        }
    }

    public void assertCanViewResults(Long userId, Long strategyId) {
        assertCanRead(userId, strategyId);
        SwotSession session = requireSession(strategyId);
        if (session.getPhase().ordinal() < SwotPhase.GENERATING_SUGGESTIONS.ordinal()) {
            throw new UnauthorizedException("Voting results are not available yet");
        }
    }

    public void assertCanReview(Long userId, Long strategyId) {
        SwotSession session = requireSession(strategyId);
        SwotParticipant participant = requireParticipant(session, userId);
        if (session.getPhase() != SwotPhase.REVIEWING) {
            throw new UnauthorizedException("Suggestion review is not open for this strategy");
        }
        if (participant.getReviewSubmittedAt() != null) {
            throw new UnauthorizedException("You have already submitted your review");
        }
    }

    /**
     * Proposing a brand-new goal under an existing area is allowed from two different roles/phases:
     * an Editor still doing their own REVIEWING-phase review, or the Owner during their own later
     * FINALIZING pass (the Owner has no peer-review step of their own — see assertCanReview).
     */
    public void assertCanProposeGoalAddition(Long userId, Long strategyId) {
        SwotSession session = requireSession(strategyId);
        if (isOwner(userId, strategyId)) {
            if (session.getPhase() != SwotPhase.FINALIZING) {
                throw new UnauthorizedException("Proposing goals is only available to the Owner during finalization");
            }
            return;
        }
        SwotParticipant participant = requireParticipant(session, userId);
        if (session.getPhase() != SwotPhase.REVIEWING) {
            throw new UnauthorizedException("Suggestion review is not open for this strategy");
        }
        if (participant.getReviewSubmittedAt() != null) {
            throw new UnauthorizedException("You have already submitted your review");
        }
    }

    public void assertCanGenerateSuggestions(Long userId, Long strategyId) {
        assertOwner(userId, strategyId);
        SwotSession session = requireSession(strategyId);
        if (session.getPhase() != SwotPhase.GENERATING_SUGGESTIONS) {
            throw new UnauthorizedException("AI suggestions cannot be (re)generated in the current phase");
        }
    }

    public void assertCanFinalize(Long userId, Long strategyId) {
        assertOwner(userId, strategyId);
        SwotSession session = requireSession(strategyId);
        if (session.getPhase() != SwotPhase.FINALIZING) {
            throw new UnauthorizedException("Finalization is not open for this strategy");
        }
    }
}
