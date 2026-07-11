package com.rit.spms.service;

import com.rit.spms.domain.*;
import com.rit.spms.domain.enums.PortfolioReviewActionType;
import com.rit.spms.domain.enums.SystemRole;
import com.rit.spms.exception.BusinessRuleException;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.exception.UnauthorizedException;
import com.rit.spms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Owns the employee goal-setting mutual-approval workflow: a leader drafts goal suggestions
 * (optionally AI-assisted), reviews them into concrete goals, submits to the employee, who
 * reviews (accept or send back) until both sides agree and the goals deploy for the year.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeGoalCycleService {

    private final EmployeeGoalCycleRepository cycleRepository;
    private final EmployeeGoalRepository goalRepository;
    private final EmployeeGoalSuggestionRepository suggestionRepository;
    private final AppUserRepository userRepository;
    private final AcademicYearRepository academicYearRepository;
    private final PortfolioCategoryRepository categoryRepository;
    private final MeasurementRepository measurementRepository;
    private final DepartmentRepository departmentRepository;
    private final PortfolioCategoryService portfolioCategoryService;
    private final PermissionService permissionService;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;
    private final AnnualEvaluationService annualEvaluationService;
    private final AnnualEvaluationNextCycleGoalRepository nextCycleGoalRepository;

    /**
     * Every active employee across every department this user heads, plus any department head
     * elsewhere whose own supervisor resolves (via the org-group chain -- see
     * {@link PermissionService#resolveSupervisor}) to this user. A department head has no entry in
     * their own department's employee list (they'd otherwise be their own report), so a Dean/Provost's
     * "direct reports" for goal-setting purposes are specifically the department heads under their
     * org hierarchy, not every individual employee several levels down.
     */
    public List<AppUser> getDirectReports(Long currentUserId) {
        List<Long> deptIds = departmentRepository.findByHeadId(currentUserId).stream().map(Department::getId).toList();
        List<AppUser> reports = new ArrayList<>(deptIds.isEmpty() ? List.of() : deptIds.stream()
                .flatMap(id -> userRepository.findByDepartmentIdAndActiveTrueOrderByFnameAscLnameAsc(id).stream())
                .filter(u -> !u.getId().equals(currentUserId))
                .toList());

        for (Department dept : departmentRepository.findAll()) {
            AppUser head = dept.getHead();
            boolean selfHeads = head != null && head.getDepartment() != null && head.getDepartment().getId().equals(dept.getId());
            if (selfHeads) {
                permissionService.resolveSupervisor(head)
                        .filter(supervisor -> supervisor.getId().equals(currentUserId))
                        .ifPresent(supervisor -> reports.add(head));
            }
        }
        return reports;
    }

    // ─── Cycle lifecycle ────────────────────────────────────────────────────

    public EmployeeGoalCycle createOrGetCycle(Long employeeId, Long academicYearId, Long currentUserId) {
        AppUser employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", employeeId));
        AcademicYear academicYear = academicYearRepository.findById(academicYearId)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", academicYearId));
        AppUser leader = permissionService.resolveAndAssertCanManageGoalsFor(currentUserId, employee);

        return cycleRepository.findByEmployeeIdAndAcademicYearId(employeeId, academicYearId)
                .orElseGet(() -> {
                    // Fail fast here (before the head types up strengths/weaknesses notes) rather
                    // than only discovering the missing categories later at suggestion-generation time.
                    portfolioCategoryService.getCategoriesForUser(employee);

                    EmployeeGoalCycle cycle = EmployeeGoalCycle.builder()
                            .employee(employee).leader(leader).academicYear(academicYear)
                            .build();
                    EmployeeGoalCycle saved = cycleRepository.save(cycle);
                    auditService.log(leader, "CREATE_GOAL_CYCLE", "EmployeeGoalCycle", saved.getId(),
                            null, "Opened goal cycle for " + employee.getFname() + " " + employee.getLname());
                    return saved;
                });
    }

    /**
     * Unused goals drafted and reviewed during a past, now-CONCLUDED Annual Evaluation for this
     * employee (see AnnualEvaluationNextCycleGoal) -- neither the head nor the employee rejected
     * them during that evaluation's own review exchange, so they're offered here instead of making
     * the head retype them. Grouped by source evaluation so the picker can label each group by the
     * academic year it came from.
     */
    public List<AnnualEvaluationNextCycleGoal> findReusableNextCycleGoals(Long employeeId) {
        return nextCycleGoalRepository.findReusableByEmployeeId(employeeId);
    }

    /**
     * Deploys previously-approved Next Cycle Goals directly, skipping the draft/review dance --
     * they already went through review and approval during the evaluation they came from. Marks
     * each source row used so it's never offered again.
     */
    public EmployeeGoalCycle useNextCycleGoals(Long employeeId, Long academicYearId, List<Long> nextCycleGoalIds, Long currentUserId) {
        AppUser employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", employeeId));
        AcademicYear academicYear = academicYearRepository.findById(academicYearId)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", academicYearId));
        AppUser leader = permissionService.resolveAndAssertCanManageGoalsFor(currentUserId, employee);

        // Merely opening Team Goal Setting for an employee/year with no cycle yet auto-creates an
        // empty DRAFT row (see GoalSettingPage's auto-createCycle effect) -- that's not "goals",
        // it's an untouched placeholder, so it shouldn't block reuse. Only a cycle with real content
        // (past DRAFT, or with notes/suggestions already on it) counts as already having goals.
        cycleRepository.findByEmployeeIdAndAcademicYearId(employeeId, academicYearId).ifPresent(existing -> {
            if (!isEmptyDraftCycle(existing)) {
                throw new BusinessRuleException("A goal cycle already exists for this employee and academic year");
            }
            cycleRepository.delete(existing);
            cycleRepository.flush();
        });
        if (nextCycleGoalIds == null || nextCycleGoalIds.isEmpty()) {
            throw new BusinessRuleException("Select at least one goal to reuse");
        }

        List<AnnualEvaluationNextCycleGoal> sourceGoals = nextCycleGoalRepository.findAllById(nextCycleGoalIds);
        if (sourceGoals.size() != nextCycleGoalIds.size()) {
            throw new BusinessRuleException("One or more selected goals could not be found");
        }
        for (AnnualEvaluationNextCycleGoal g : sourceGoals) {
            if (!g.getEvaluation().getEmployee().getId().equals(employeeId)) {
                throw new BusinessRuleException("Goal does not belong to this employee");
            }
            if (Boolean.TRUE.equals(g.getUsed())) {
                throw new BusinessRuleException("'" + g.getSuggestedTitle() + "' has already been used for another cycle");
            }
        }

        EmployeeGoalCycle cycle = EmployeeGoalCycle.builder()
                .employee(employee).leader(leader).academicYear(academicYear)
                .state(EmployeeGoalCycle.CycleState.DEPLOYED)
                .employeeAcceptedAt(LocalDateTime.now())
                .build();
        EmployeeGoalCycle saved = cycleRepository.save(cycle);

        int sortOrder = 0;
        for (AnnualEvaluationNextCycleGoal g : sourceGoals) {
            String title = !isBlank(g.getEmployeeEditedTitle()) ? g.getEmployeeEditedTitle()
                    : !isBlank(g.getLeaderEditedTitle()) ? g.getLeaderEditedTitle()
                    : g.getSuggestedTitle();
            String description = g.getEmployeeEditedDescription() != null ? g.getEmployeeEditedDescription()
                    : g.getLeaderEditedDescription() != null ? g.getLeaderEditedDescription()
                    : g.getSuggestedDescription();

            goalRepository.save(EmployeeGoal.builder()
                    .cycle(saved).category(g.getCategory())
                    .goalTitle(title).description(description)
                    .rubricUnsatisfactory(g.getRubricUnsatisfactory())
                    .rubricMeetsExpectations(g.getRubricMeetsExpectations())
                    .rubricExceedsExpectations(g.getRubricExceedsExpectations())
                    .employeeActionType(PortfolioReviewActionType.APPROVE_AS_IS)
                    .employeeReviewedAt(LocalDateTime.now())
                    .sortOrder(sortOrder++)
                    .build());

            g.setUsed(true);
            g.setUsedInCycle(saved);
            nextCycleGoalRepository.save(g);
        }

        auditService.log(leader, "USE_NEXT_CYCLE_GOALS", "EmployeeGoalCycle", saved.getId(),
                null, "Deployed " + sourceGoals.size() + " goal(s) reused from a prior evaluation for "
                        + employee.getFname() + " " + employee.getLname());
        annualEvaluationService.backfillGoalResultsForDeployedCycle(saved);
        eventPublisher.publishEvent(new GoalCycleDeployedEvent(saved.getId(), leader.getId(), employee.getId()));
        return saved;
    }

    /**
     * Batch version of {@link #useNextCycleGoals} -- for every one of this head's direct reports
     * (see {@link #getDirectReports}, the same list Team Goal Setting's employee dropdown shows),
     * checks whether they have a concluded evaluation from {@code sourceAcademicYearId} with unused
     * Next Cycle Goals AND no existing goal cycle yet for {@code targetAcademicYearId}; if both
     * hold, deploys every eligible goal from that evaluation into a new cycle for the target year,
     * exactly like the single-employee reuse flow. Employees who already have a cycle for the
     * target year, or have no eligible goals from the source year, are skipped (not an error) and
     * reported back so the head can see why each one was or wasn't touched.
     */
    public List<BatchReuseOutcome> batchUseNextCycleGoals(Long targetAcademicYearId, Long sourceAcademicYearId, Long currentUserId) {
        academicYearRepository.findById(targetAcademicYearId)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", targetAcademicYearId));
        academicYearRepository.findById(sourceAcademicYearId)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", sourceAcademicYearId));

        List<BatchReuseOutcome> results = new ArrayList<>();
        for (AppUser employee : getDirectReports(currentUserId)) {
            boolean hasRealCycle = cycleRepository.findByEmployeeIdAndAcademicYearId(employee.getId(), targetAcademicYearId)
                    .filter(c -> !isEmptyDraftCycle(c))
                    .isPresent();
            if (hasRealCycle) {
                results.add(new BatchReuseOutcome(employee, BatchReuseOutcome.Status.ALREADY_HAS_GOALS, 0, null));
                continue;
            }
            List<AnnualEvaluationNextCycleGoal> candidates =
                    nextCycleGoalRepository.findReusableByEmployeeIdAndSourceAcademicYearId(employee.getId(), sourceAcademicYearId);
            if (candidates.isEmpty()) {
                results.add(new BatchReuseOutcome(employee, BatchReuseOutcome.Status.NO_ELIGIBLE_GOALS, 0, null));
                continue;
            }
            List<Long> ids = candidates.stream().map(AnnualEvaluationNextCycleGoal::getId).toList();
            EmployeeGoalCycle cycle = useNextCycleGoals(employee.getId(), targetAcademicYearId, ids, currentUserId);
            results.add(new BatchReuseOutcome(employee, BatchReuseOutcome.Status.DEPLOYED, candidates.size(), cycle));
        }
        return results;
    }

    @lombok.Getter
    @lombok.AllArgsConstructor
    public static class BatchReuseOutcome {
        private final AppUser employee;
        private final Status status;
        private final int goalsDeployed;
        private final EmployeeGoalCycle cycle;

        public enum Status { DEPLOYED, ALREADY_HAS_GOALS, NO_ELIGIBLE_GOALS }
    }

    public EmployeeGoalCycle getCycle(Long cycleId, Long currentUserId) {
        EmployeeGoalCycle cycle = requireCycle(cycleId);
        assertCanView(cycle, currentUserId);
        return cycle;
    }

    public List<EmployeeGoalCycle> getMyCycles(Long employeeId) {
        return cycleRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
    }

    public List<EmployeeGoalCycle> getTeamCycles(Long leaderId, Long academicYearId) {
        return cycleRepository.findByLeaderIdAndAcademicYearIdOrderByCreatedAtDesc(leaderId, academicYearId);
    }

    public EmployeeGoalCycle updateNotes(Long cycleId, String strengths, String weaknesses, Long currentUserId) {
        EmployeeGoalCycle cycle = requireCycle(cycleId);
        assertLeaderOrAdmin(cycle, currentUserId);
        assertState(cycle, "update notes", EmployeeGoalCycle.CycleState.DRAFT, EmployeeGoalCycle.CycleState.EMPLOYEE_SUBMITTED);
        cycle.setLeaderStrengths(strengths);
        cycle.setLeaderWeaknesses(weaknesses);
        return cycleRepository.save(cycle);
    }

    // ─── Leader stage: AI suggestions ───────────────────────────────────────

    /**
     * Permission/state gate + category lookup only -- deliberately NOT the place that calls
     * recordGenerationRequested/generateSuggestionsAsync. Those two must be invoked directly from
     * the controller (a non-transactional caller), each getting its own transaction that commits
     * independently, exactly like SwotSuggestionController does. If they were called from within
     * this method instead (which carries its own class-level @Transactional), recordGenerationRequested's
     * write would join that ambient transaction and only commit when this method returns -- by which
     * time the @Async generator may already be running on another thread/transaction, reading the
     * cycle before the commit and later overwriting generationRequestedAt back to its stale value
     * when it saves its own results (Hibernate's default full-column UPDATE has no way to know that
     * field changed elsewhere). See EmployeeGoalCycleController.generateSuggestions.
     */
    public List<PortfolioCategory> assertCanGenerateSuggestions(Long cycleId, Long currentUserId) {
        EmployeeGoalCycle cycle = requireCycle(cycleId);
        assertLeaderOrAdmin(cycle, currentUserId);
        assertState(cycle, "generate AI suggestions", EmployeeGoalCycle.CycleState.DRAFT);
        return portfolioCategoryService.getCategoriesForUser(cycle.getEmployee());
    }

    public List<EmployeeGoalSuggestion> getSuggestions(Long cycleId, Long currentUserId) {
        EmployeeGoalCycle cycle = requireCycle(cycleId);
        assertLeaderOrAdmin(cycle, currentUserId);
        return suggestionRepository.findByCycleIdOrderBySortOrder(cycleId);
    }

    public EmployeeGoalSuggestion reviewSuggestion(Long cycleId, Long suggestionId, PortfolioReviewActionType action,
                                                    String editedTitle, String editedDescription, Long currentUserId) {
        EmployeeGoalCycle cycle = requireCycle(cycleId);
        assertLeaderOrAdmin(cycle, currentUserId);
        assertState(cycle, "review a suggestion", EmployeeGoalCycle.CycleState.DRAFT);
        EmployeeGoalSuggestion suggestion = requireSuggestion(cycleId, suggestionId);
        suggestion.setLeaderActionType(action);
        suggestion.setEditedTitle(editedTitle);
        suggestion.setEditedDescription(editedDescription);
        suggestion.setLeaderReviewedAt(LocalDateTime.now());
        return suggestionRepository.save(suggestion);
    }

    /** "Add a new goal" -- a leader-authored suggestion, pre-approved as-is. */
    public EmployeeGoalSuggestion addSuggestion(Long cycleId, Long categoryId, String title, String description,
                                                 String rubricUnsatisfactory, String rubricMeetsExpectations,
                                                 String rubricExceedsExpectations, Long currentUserId) {
        EmployeeGoalCycle cycle = requireCycle(cycleId);
        assertLeaderOrAdmin(cycle, currentUserId);
        assertState(cycle, "add a goal", EmployeeGoalCycle.CycleState.DRAFT);
        PortfolioCategory category = requireCategoryForEmployee(cycle.getEmployee(), categoryId);

        int nextSort = suggestionRepository.findByCycleIdOrderBySortOrder(cycleId).size();
        EmployeeGoalSuggestion suggestion = EmployeeGoalSuggestion.builder()
                .cycle(cycle).category(category)
                .suggestedTitle(title).suggestedDescription(description)
                .rubricUnsatisfactory(rubricUnsatisfactory)
                .rubricMeetsExpectations(rubricMeetsExpectations)
                .rubricExceedsExpectations(rubricExceedsExpectations)
                .generatedByModel(null)
                .sortOrder(nextSort)
                .leaderActionType(PortfolioReviewActionType.APPROVE_AS_IS)
                .leaderReviewedAt(LocalDateTime.now())
                .build();
        return suggestionRepository.save(suggestion);
    }

    /** Rubric edits are independent of the accept/reject decision, so this is separate from {@link #reviewSuggestion}. */
    public EmployeeGoalSuggestion updateSuggestionRubric(Long cycleId, Long suggestionId, String rubricUnsatisfactory,
                                                          String rubricMeetsExpectations, String rubricExceedsExpectations,
                                                          Long currentUserId) {
        EmployeeGoalCycle cycle = requireCycle(cycleId);
        assertLeaderOrAdmin(cycle, currentUserId);
        assertState(cycle, "edit a goal's rubric", EmployeeGoalCycle.CycleState.DRAFT);
        EmployeeGoalSuggestion suggestion = requireSuggestion(cycleId, suggestionId);
        suggestion.setRubricUnsatisfactory(rubricUnsatisfactory);
        suggestion.setRubricMeetsExpectations(rubricMeetsExpectations);
        suggestion.setRubricExceedsExpectations(rubricExceedsExpectations);
        return suggestionRepository.save(suggestion);
    }

    public void deleteSuggestion(Long cycleId, Long suggestionId, Long currentUserId) {
        EmployeeGoalCycle cycle = requireCycle(cycleId);
        assertLeaderOrAdmin(cycle, currentUserId);
        assertState(cycle, "remove a goal", EmployeeGoalCycle.CycleState.DRAFT);
        EmployeeGoalSuggestion suggestion = requireSuggestion(cycleId, suggestionId);
        suggestionRepository.delete(suggestion);
    }

    /** Materializes every non-rejected suggestion into a concrete goal and submits to the employee. */
    public EmployeeGoalCycle submitForReview(Long cycleId, Long currentUserId) {
        EmployeeGoalCycle cycle = requireCycle(cycleId);
        assertLeaderOrAdmin(cycle, currentUserId);
        assertState(cycle, "submit for employee review", EmployeeGoalCycle.CycleState.DRAFT);

        List<EmployeeGoalSuggestion> suggestions = suggestionRepository.findByCycleIdOrderBySortOrder(cycleId);
        if (suggestions.isEmpty()) {
            throw new BusinessRuleException("Add or generate at least one goal before submitting");
        }
        for (EmployeeGoalSuggestion s : suggestions) {
            if (s.getLeaderActionType() == null) {
                throw new BusinessRuleException("You must review every suggested goal before submitting (missing: " + s.getSuggestedTitle() + ")");
            }
        }
        for (EmployeeGoalSuggestion s : suggestions) {
            if (s.getLeaderActionType() != PortfolioReviewActionType.REJECT) {
                assertRubricComplete(s.getSuggestedTitle(), s.getRubricUnsatisfactory(), s.getRubricMeetsExpectations(), s.getRubricExceedsExpectations());
            }
        }

        int sortOrder = 0;
        for (EmployeeGoalSuggestion s : suggestions) {
            if (s.getLeaderActionType() == PortfolioReviewActionType.REJECT) {
                continue;
            }
            boolean useEdited = s.getLeaderActionType() == PortfolioReviewActionType.APPROVE_WITH_EDITS
                    || s.getLeaderActionType() == PortfolioReviewActionType.PROPOSE_ALTERNATIVE;
            String title = useEdited && s.getEditedTitle() != null && !s.getEditedTitle().isBlank() ? s.getEditedTitle() : s.getSuggestedTitle();
            String description = useEdited && s.getEditedDescription() != null ? s.getEditedDescription() : s.getSuggestedDescription();

            goalRepository.save(EmployeeGoal.builder()
                    .cycle(cycle).category(s.getCategory())
                    .goalTitle(title).description(description)
                    .rubricUnsatisfactory(s.getRubricUnsatisfactory())
                    .rubricMeetsExpectations(s.getRubricMeetsExpectations())
                    .rubricExceedsExpectations(s.getRubricExceedsExpectations())
                    .sortOrder(sortOrder++)
                    .build());
        }

        cycle.setState(EmployeeGoalCycle.CycleState.LEADER_SUBMITTED);
        cycle.setLeaderSubmittedAt(LocalDateTime.now());
        EmployeeGoalCycle saved = cycleRepository.save(cycle);
        auditService.log(cycle.getLeader(), "SUBMIT_GOAL_CYCLE_FOR_REVIEW", "EmployeeGoalCycle", cycleId,
                null, "DRAFT", "LEADER_SUBMITTED", "Submitted goal cycle for employee review");
        eventPublisher.publishEvent(new GoalCycleSubmittedEvent(cycleId, cycle.getEmployee().getId(), cycle.getLeader().getId()));
        return saved;
    }

    // ─── Leader stage: direct goal edits after employee sends back ─────────

    public EmployeeGoal updateGoal(Long cycleId, Long goalId, String title, String description,
                                    Long categoryId, Long measurementId, String rubricUnsatisfactory,
                                    String rubricMeetsExpectations, String rubricExceedsExpectations, Long currentUserId) {
        EmployeeGoalCycle cycle = requireCycle(cycleId);
        assertLeaderOrAdmin(cycle, currentUserId);
        assertState(cycle, "edit a goal", EmployeeGoalCycle.CycleState.EMPLOYEE_SUBMITTED);
        EmployeeGoal goal = requireGoal(cycleId, goalId);
        goal.setGoalTitle(title);
        goal.setDescription(description);
        if (categoryId != null) {
            goal.setCategory(requireCategoryForEmployee(cycle.getEmployee(), categoryId));
        }
        if (measurementId != null) {
            goal.setMeasurement(measurementRepository.findById(measurementId)
                    .orElseThrow(() -> new ResourceNotFoundException("Measurement", measurementId)));
        }
        goal.setRubricUnsatisfactory(rubricUnsatisfactory);
        goal.setRubricMeetsExpectations(rubricMeetsExpectations);
        goal.setRubricExceedsExpectations(rubricExceedsExpectations);
        return goalRepository.save(goal);
    }

    public EmployeeGoal addGoal(Long cycleId, Long categoryId, String title, String description,
                                 String rubricUnsatisfactory, String rubricMeetsExpectations,
                                 String rubricExceedsExpectations, Long currentUserId) {
        EmployeeGoalCycle cycle = requireCycle(cycleId);
        assertLeaderOrAdmin(cycle, currentUserId);
        assertState(cycle, "add a goal", EmployeeGoalCycle.CycleState.EMPLOYEE_SUBMITTED);
        PortfolioCategory category = requireCategoryForEmployee(cycle.getEmployee(), categoryId);
        int nextSort = goalRepository.findByCycleIdOrderBySortOrder(cycleId).size();
        return goalRepository.save(EmployeeGoal.builder()
                .cycle(cycle).category(category).goalTitle(title).description(description)
                .rubricUnsatisfactory(rubricUnsatisfactory)
                .rubricMeetsExpectations(rubricMeetsExpectations)
                .rubricExceedsExpectations(rubricExceedsExpectations)
                .sortOrder(nextSort).build());
    }

    public void deleteGoal(Long cycleId, Long goalId, Long currentUserId) {
        EmployeeGoalCycle cycle = requireCycle(cycleId);
        assertLeaderOrAdmin(cycle, currentUserId);
        assertState(cycle, "remove a goal", EmployeeGoalCycle.CycleState.EMPLOYEE_SUBMITTED);
        EmployeeGoal goal = requireGoal(cycleId, goalId);
        goalRepository.delete(goal);
    }

    /** Fixes the "stuck forever" bug: resubmits directly from EMPLOYEE_SUBMITTED, no detour through DRAFT. */
    public EmployeeGoalCycle resubmitForReview(Long cycleId, Long currentUserId) {
        EmployeeGoalCycle cycle = requireCycle(cycleId);
        assertLeaderOrAdmin(cycle, currentUserId);
        assertState(cycle, "resubmit for employee review", EmployeeGoalCycle.CycleState.EMPLOYEE_SUBMITTED);

        List<EmployeeGoal> goals = goalRepository.findByCycleIdOrderBySortOrder(cycleId);
        for (EmployeeGoal goal : goals) {
            assertRubricComplete(goal.getGoalTitle(), goal.getRubricUnsatisfactory(),
                    goal.getRubricMeetsExpectations(), goal.getRubricExceedsExpectations());
        }

        for (EmployeeGoal goal : goals) {
            goal.setEmployeeActionType(null);
            goal.setEmployeeEditedTitle(null);
            goal.setEmployeeEditedDescription(null);
            goal.setEmployeeReviewedAt(null);
            goalRepository.save(goal);
        }

        cycle.setState(EmployeeGoalCycle.CycleState.LEADER_SUBMITTED);
        cycle.setLeaderSubmittedAt(LocalDateTime.now());
        EmployeeGoalCycle saved = cycleRepository.save(cycle);
        auditService.log(cycle.getLeader(), "RESUBMIT_GOAL_CYCLE_FOR_REVIEW", "EmployeeGoalCycle", cycleId,
                null, "EMPLOYEE_SUBMITTED", "LEADER_SUBMITTED", "Resubmitted goal cycle for employee review");
        eventPublisher.publishEvent(new GoalCycleSubmittedEvent(cycleId, cycle.getEmployee().getId(), cycle.getLeader().getId()));
        return saved;
    }

    // ─── Employee stage ─────────────────────────────────────────────────────

    public List<EmployeeGoal> getCycleGoals(Long cycleId, Long currentUserId) {
        EmployeeGoalCycle cycle = requireCycle(cycleId);
        assertCanView(cycle, currentUserId);
        return goalRepository.findByCycleIdOrderBySortOrder(cycleId);
    }

    public EmployeeGoalCycle startEmployeeReview(Long cycleId, Long currentUserId) {
        EmployeeGoalCycle cycle = requireCycle(cycleId);
        assertEmployee(cycle, currentUserId);
        assertState(cycle, "start your review", EmployeeGoalCycle.CycleState.LEADER_SUBMITTED);
        cycle.setState(EmployeeGoalCycle.CycleState.EMPLOYEE_REVIEW);
        return cycleRepository.save(cycle);
    }

    public EmployeeGoal reviewGoal(Long cycleId, Long goalId, PortfolioReviewActionType action,
                                    String editedTitle, String editedDescription, Long currentUserId) {
        EmployeeGoalCycle cycle = requireCycle(cycleId);
        assertEmployee(cycle, currentUserId);
        assertState(cycle, "review a goal", EmployeeGoalCycle.CycleState.EMPLOYEE_REVIEW);
        if (action == PortfolioReviewActionType.REJECT) {
            throw new BusinessRuleException("Employees cannot reject a goal -- accept it, accept with edits, or propose an alternative and submit back for more consideration");
        }
        EmployeeGoal goal = requireGoal(cycleId, goalId);
        goal.setEmployeeActionType(action);
        goal.setEmployeeEditedTitle(editedTitle);
        goal.setEmployeeEditedDescription(editedDescription);
        goal.setEmployeeReviewedAt(LocalDateTime.now());
        return goalRepository.save(goal);
    }

    public EmployeeGoalCycle acceptCycle(Long cycleId, String signatureName, Long currentUserId) {
        EmployeeGoalCycle cycle = requireCycle(cycleId);
        assertEmployee(cycle, currentUserId);
        assertState(cycle, "accept these goals", EmployeeGoalCycle.CycleState.EMPLOYEE_REVIEW);
        if (signatureName == null || signatureName.isBlank()) {
            throw new BusinessRuleException("Type your name to sign before accepting these goals");
        }

        List<EmployeeGoal> goals = goalRepository.findByCycleIdOrderBySortOrder(cycleId);
        for (EmployeeGoal goal : goals) {
            if (goal.getEmployeeActionType() == null) {
                throw new BusinessRuleException("You must review every goal before accepting (missing: " + goal.getGoalTitle() + ")");
            }
        }
        for (EmployeeGoal goal : goals) {
            if (goal.getEmployeeActionType() != PortfolioReviewActionType.APPROVE_AS_IS) {
                if (goal.getEmployeeEditedTitle() != null && !goal.getEmployeeEditedTitle().isBlank()) {
                    goal.setGoalTitle(goal.getEmployeeEditedTitle());
                }
                if (goal.getEmployeeEditedDescription() != null) {
                    goal.setDescription(goal.getEmployeeEditedDescription());
                }
                goalRepository.save(goal);
            }
        }

        cycle.setState(EmployeeGoalCycle.CycleState.DEPLOYED);
        cycle.setEmployeeAcceptedAt(LocalDateTime.now());
        cycle.setEmployeeSignatureName(signatureName);
        EmployeeGoalCycle saved = cycleRepository.save(cycle);
        // The employee's Annual Evaluation for this year may already exist (created before goal-setting
        // finished) -- backfill its goal results now that these goals are actually deployed.
        annualEvaluationService.backfillGoalResultsForDeployedCycle(saved);
        auditService.log(cycle.getEmployee(), "ACCEPT_GOAL_CYCLE", "EmployeeGoalCycle", cycleId,
                null, "EMPLOYEE_REVIEW", "DEPLOYED", "Employee accepted goals for the academic year, signed as \"" + signatureName + "\"");
        eventPublisher.publishEvent(new GoalCycleDeployedEvent(cycleId, cycle.getLeader().getId(), cycle.getEmployee().getId()));
        return saved;
    }

    public EmployeeGoalCycle submitBack(Long cycleId, Long currentUserId) {
        EmployeeGoalCycle cycle = requireCycle(cycleId);
        assertEmployee(cycle, currentUserId);
        assertState(cycle, "submit these goals back", EmployeeGoalCycle.CycleState.EMPLOYEE_REVIEW);
        cycle.setState(EmployeeGoalCycle.CycleState.EMPLOYEE_SUBMITTED);
        EmployeeGoalCycle saved = cycleRepository.save(cycle);
        auditService.log(cycle.getEmployee(), "SUBMIT_GOAL_CYCLE_BACK", "EmployeeGoalCycle", cycleId,
                null, "EMPLOYEE_REVIEW", "EMPLOYEE_SUBMITTED", "Employee sent goals back for more consideration");
        eventPublisher.publishEvent(new GoalCycleSentBackEvent(cycleId, cycle.getLeader().getId(), cycle.getEmployee().getId()));
        return saved;
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private EmployeeGoalCycle requireCycle(Long cycleId) {
        return cycleRepository.findById(cycleId)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeGoalCycle", cycleId));
    }

    private EmployeeGoalSuggestion requireSuggestion(Long cycleId, Long suggestionId) {
        EmployeeGoalSuggestion suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeGoalSuggestion", suggestionId));
        if (!suggestion.getCycle().getId().equals(cycleId)) {
            throw new BusinessRuleException("Suggestion does not belong to this cycle");
        }
        return suggestion;
    }

    private EmployeeGoal requireGoal(Long cycleId, Long goalId) {
        EmployeeGoal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeGoal", goalId));
        if (!goal.getCycle().getId().equals(cycleId)) {
            throw new BusinessRuleException("Goal does not belong to this cycle");
        }
        return goal;
    }

    private PortfolioCategory requireCategoryForEmployee(AppUser employee, Long categoryId) {
        PortfolioCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("PortfolioCategory", categoryId));
        boolean valid = portfolioCategoryService.getCategoriesForUser(employee).stream()
                .anyMatch(c -> c.getId().equals(categoryId));
        if (!valid) {
            throw new BusinessRuleException("Category '" + category.getCategoryName() + "' does not apply to this employee's title");
        }
        return category;
    }

    /**
     * Also used to decide whether leaderStrengths/leaderWeaknesses may be included in a
     * CycleResponse -- those are the leader's private notes about the employee and must never
     * reach the employee themselves, any other head, or anyone up/down the org hierarchy.
     */
    public boolean isLeaderOrAdmin(EmployeeGoalCycle cycle, Long currentUserId) {
        AppUser currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));
        return cycle.getLeader().getId().equals(currentUserId) || currentUser.hasRole(SystemRole.ADMIN);
    }

    private void assertLeaderOrAdmin(EmployeeGoalCycle cycle, Long currentUserId) {
        if (!isLeaderOrAdmin(cycle, currentUserId)) {
            throw new UnauthorizedException("Only this employee's department head or an admin can manage this goal cycle");
        }
    }

    private void assertEmployee(EmployeeGoalCycle cycle, Long currentUserId) {
        if (!cycle.getEmployee().getId().equals(currentUserId)) {
            throw new UnauthorizedException("Only the employee this cycle belongs to can perform this action");
        }
    }

    private void assertCanView(EmployeeGoalCycle cycle, Long currentUserId) {
        AppUser currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));
        boolean allowed = cycle.getEmployee().getId().equals(currentUserId)
                || cycle.getLeader().getId().equals(currentUserId)
                || currentUser.hasRole(SystemRole.ADMIN);
        if (!allowed) {
            throw new UnauthorizedException("You do not have access to this goal cycle");
        }
    }

    private void assertState(EmployeeGoalCycle cycle, String action, EmployeeGoalCycle.CycleState... allowed) {
        for (EmployeeGoalCycle.CycleState s : allowed) {
            if (cycle.getState() == s) return;
        }
        throw new BusinessRuleException("Cannot " + action + " while the cycle is in state " + cycle.getState());
    }

    /**
     * True for a DRAFT cycle nobody has actually put anything into yet -- no notes, no suggestions
     * (goals only ever get materialized once the cycle leaves DRAFT, so there's nothing else to
     * check). Selecting an employee/year in Team Goal Setting auto-creates exactly this kind of row
     * the moment there's no cycle yet, so its mere existence can't be treated as "already has goals".
     */
    private boolean isEmptyDraftCycle(EmployeeGoalCycle cycle) {
        if (cycle.getState() != EmployeeGoalCycle.CycleState.DRAFT) return false;
        if (!isBlank(cycle.getLeaderStrengths()) || !isBlank(cycle.getLeaderWeaknesses())) return false;
        return suggestionRepository.findByCycleIdOrderBySortOrder(cycle.getId()).isEmpty();
    }

    /** All 3 rubric levels must be filled in before a goal can be handed to the employee for review. */
    private void assertRubricComplete(String goalTitle, String rubricUnsatisfactory, String rubricMeetsExpectations, String rubricExceedsExpectations) {
        if (isBlank(rubricUnsatisfactory) || isBlank(rubricMeetsExpectations) || isBlank(rubricExceedsExpectations)) {
            throw new BusinessRuleException("Every goal needs all 3 rubric levels filled in before submitting (missing: " + goalTitle + ")");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
