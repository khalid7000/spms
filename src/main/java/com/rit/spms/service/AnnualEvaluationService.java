package com.rit.spms.service;

import com.rit.spms.domain.*;
import com.rit.spms.domain.enums.AnnualEvaluationState;
import com.rit.spms.exception.BusinessRuleException;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.exception.UnauthorizedException;
import com.rit.spms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * End-of-year evaluation: employee tags achievements to criteria and self-assesses each category,
 * then the head rates every criterion/category plus an overall rank and submits-and-signs in one
 * action, then the employee signs (or refuses to sign with a rationale). Optionally, before
 * submitting-and-signing, the head may send the evaluation back to the employee exactly once via
 * {@link #returnToEmployeeForReview} for another round of edits/comments -- see {@code
 * RETURNED_TO_EMPLOYEE}. The employee may only edit while DRAFT or RETURNED_TO_EMPLOYEE; the head
 * may edit any time from EMPLOYEE_SUBMITTED onward as long as {@link AnnualEvaluation#isLocked()}
 * is false -- since submitting and signing now happen together, HEAD_SUBMITTED always implies the
 * head has already signed, so the head's edit window effectively closes the moment they submit.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AnnualEvaluationService {

    private final AnnualEvaluationRepository evaluationRepository;
    private final AnnualEvaluationCategoryResultRepository categoryResultRepository;
    private final AnnualEvaluationCriteriaResultRepository criteriaResultRepository;
    private final AnnualEvaluationGoalResultRepository goalResultRepository;
    private final AppUserRepository userRepository;
    private final AcademicYearRepository academicYearRepository;
    private final PortfolioCategoryService portfolioCategoryService;
    private final CategoryCriteriaRepository criteriaRepository;
    private final PortfolioEntryRepository entryRepository;
    private final EmployeeGoalCycleRepository goalCycleRepository;
    private final EmployeeGoalRepository goalRepository;
    private final AnnualEvaluationNextCycleGoalRepository nextCycleGoalRepository;
    private final PermissionService permissionService;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditService auditService;

    // ─── Creation ───────────────────────────────────────────────────────────────────────

    public AnnualEvaluation getOrCreateForEmployeeAndYear(Long employeeId, Long academicYearId, Long currentUserId) {
        AppUser employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", employeeId));
        AcademicYear academicYear = academicYearRepository.findById(academicYearId)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", academicYearId));

        return evaluationRepository.findByEmployeeIdAndAcademicYearId(employeeId, academicYearId)
                .orElseGet(() -> {
                    AppUser head = resolveHead(employee);
                    assertCanView(employee, head, currentUserId);
                    return createEvaluation(employee, head, academicYear);
                });
    }

    /**
     * Used by {@link AcademicYearService} to auto-seed a DRAFT evaluation for every eligible user
     * on a new academic year. Eligibility is checked with non-throwing lookups (not a try/catch
     * around {@link #createEvaluation}) -- see {@link PortfolioCategoryService#hasCategoriesForUser}
     * for why a caught-and-swallowed exception here would still poison the whole batch's transaction.
     */
    public void autoCreateForNewAcademicYear(AppUser employee, AcademicYear academicYear) {
        if (evaluationRepository.existsByEmployeeIdAndAcademicYearId(employee.getId(), academicYear.getId())) {
            return;
        }
        AppUser head;
        try {
            head = resolveHead(employee);
        } catch (BusinessRuleException e) {
            return; // no department head resolvable -- skip, matches per-title/per-head gating used elsewhere
        }
        if (!portfolioCategoryService.hasCategoriesForUser(employee)) {
            return; // no portfolio categories configured for this title yet -- skip, admin can configure later
        }
        createEvaluation(employee, head, academicYear);
    }

    private AppUser resolveHead(AppUser employee) {
        return permissionService.resolveSupervisor(employee)
                .orElseThrow(() -> new BusinessRuleException("Employee has no supervisor assigned; cannot create an annual evaluation"));
    }

    private AnnualEvaluation createEvaluation(AppUser employee, AppUser head, AcademicYear academicYear) {
        List<PortfolioCategory> categories = portfolioCategoryService.getCategoriesForUser(employee);

        AnnualEvaluation evaluation = evaluationRepository.save(AnnualEvaluation.builder()
                .employee(employee).head(head).academicYear(academicYear)
                .build());

        for (PortfolioCategory category : categories) {
            categoryResultRepository.save(AnnualEvaluationCategoryResult.builder()
                    .evaluation(evaluation).category(category).build());
        }

        // Goal-result rows are seeded only if a DEPLOYED goal cycle exists for this employee/year --
        // goal-setting is optional/separate from the evaluation, so there may be none yet.
        goalCycleRepository.findByEmployeeIdAndAcademicYearId(employee.getId(), academicYear.getId())
                .filter(cycle -> cycle.getState() == EmployeeGoalCycle.CycleState.DEPLOYED)
                .ifPresent(cycle -> {
                    for (EmployeeGoal goal : goalRepository.findByCycleIdOrderBySortOrder(cycle.getId())) {
                        goalResultRepository.save(AnnualEvaluationGoalResult.builder()
                                .evaluation(evaluation).goal(goal).build());
                    }
                });

        auditService.log(head, "CREATE_ANNUAL_EVALUATION", "AnnualEvaluation", evaluation.getId(),
                null, "Opened annual evaluation for " + employee.getFname() + " " + employee.getLname()
                        + " (" + academicYear.getName() + ")");
        return evaluation;
    }

    /**
     * Seeds any missing {@link AnnualEvaluationGoalResult} rows on an already-existing evaluation
     * once a goal cycle deploys -- {@link #createEvaluation} only seeds goal results from a cycle
     * that's already DEPLOYED at that moment, so an evaluation created (lazily, or in the year's
     * batch auto-create) before goal-setting finished would otherwise never pick up its goals.
     * No-op if the employee has no evaluation yet for this cycle's year (nothing to backfill onto).
     */
    public void backfillGoalResultsForDeployedCycle(EmployeeGoalCycle cycle) {
        backfillGoalResultsForDeployedCycleCounted(cycle);
    }

    private int backfillGoalResultsForDeployedCycleCounted(EmployeeGoalCycle cycle) {
        return evaluationRepository.findByEmployeeIdAndAcademicYearId(cycle.getEmployee().getId(), cycle.getAcademicYear().getId())
                .map(evaluation -> {
                    int created = 0;
                    for (EmployeeGoal goal : goalRepository.findByCycleIdOrderBySortOrder(cycle.getId())) {
                        if (goalResultRepository.findByEvaluationIdAndGoalId(evaluation.getId(), goal.getId()).isEmpty()) {
                            goalResultRepository.save(AnnualEvaluationGoalResult.builder()
                                    .evaluation(evaluation).goal(goal).build());
                            created++;
                        }
                    }
                    return created;
                })
                .orElse(0);
    }

    /**
     * One-off repair for evaluations created (including ones already CONCLUDED) before this
     * backfill existed, or before their goal cycle deployed -- runs the same backfill as {@link
     * #backfillGoalResultsForDeployedCycle} for every currently-DEPLOYED cycle org-wide. Safe to
     * re-run: skips any goal that already has a result row. Backfilled goals show up with a null
     * head rank (never rated) on already-concluded evaluations -- expected, not an error.
     */
    public int backfillAllMissingGoalResults() {
        int created = 0;
        for (EmployeeGoalCycle cycle : goalCycleRepository.findByState(EmployeeGoalCycle.CycleState.DEPLOYED)) {
            created += backfillGoalResultsForDeployedCycleCounted(cycle);
        }
        return created;
    }

    /**
     * One-off repair for evaluations created back when a self-heading department head's supervisor
     * incorrectly resolved to themselves (their department's head IS them -- see the fix in {@link
     * PermissionService#resolveSupervisor}, which now falls back to the org group chain). Reassigns
     * `head` on any evaluation still stuck with employee == head to the correctly-resolved
     * supervisor. Safe to re-run: no-ops on evaluations that are already correct or that still have
     * no resolvable supervisor above them.
     */
    public int repairSelfHeadedEvaluations() {
        int fixed = 0;
        for (AnnualEvaluation evaluation : evaluationRepository.findAll()) {
            if (!evaluation.getEmployee().getId().equals(evaluation.getHead().getId())) {
                continue;
            }
            Long fixedId = permissionService.resolveSupervisor(evaluation.getEmployee())
                    .filter(supervisor -> !supervisor.getId().equals(evaluation.getEmployee().getId()))
                    .map(supervisor -> {
                        evaluation.setHead(supervisor);
                        evaluationRepository.save(evaluation);
                        return evaluation.getId();
                    })
                    .orElse(null);
            if (fixedId != null) {
                fixed++;
            }
        }
        return fixed;
    }

    // ─── Lookups ────────────────────────────────────────────────────────────────────────

    public AnnualEvaluation getById(Long evaluationId, Long currentUserId) {
        AnnualEvaluation evaluation = requireEvaluation(evaluationId);
        assertCanView(evaluation.getEmployee(), evaluation.getHead(), currentUserId);
        return evaluation;
    }

    public List<AnnualEvaluation> getForHeadAndYear(Long headId, Long academicYearId) {
        return evaluationRepository.findByHeadIdAndAcademicYearIdOrderByCreatedAtDesc(headId, academicYearId);
    }

    /**
     * Read-only rollup for a head at any level: every evaluation belonging to an employee in a
     * department the given user heads directly, or in a department under an org group they head
     * (recursively through sub-groups) -- e.g. a Dean over several departments sees every
     * department's evaluation statuses, not just the ones where they're literally the rater.
     */
    public List<AnnualEvaluation> getHierarchyEvaluations(Long userId, Long academicYearId) {
        java.util.Set<Long> departmentIds = permissionService.resolveHierarchyDepartmentIds(userId);
        if (departmentIds.isEmpty()) {
            return List.of();
        }
        return evaluationRepository.findByEmployee_Department_IdInAndAcademicYearId(departmentIds, academicYearId).stream()
                .sorted(java.util.Comparator
                        .comparing((AnnualEvaluation e) -> e.getEmployee().getDepartment().getName())
                        .thenComparing(e -> e.getEmployee().getFname()))
                .toList();
    }

    /** Admin/HR report search -- every concluded evaluation for a year, org-wide (not scoped to a specific head). */
    public List<AnnualEvaluation> getConcludedForYear(Long academicYearId) {
        return evaluationRepository.findByAcademicYearIdAndStateOrderByEmployeeId(academicYearId, AnnualEvaluationState.CONCLUDED);
    }

    public List<AnnualEvaluationCategoryResult> getCategoryResults(Long evaluationId) {
        return categoryResultRepository.findByEvaluationId(evaluationId);
    }

    /**
     * Every criteria under the evaluation's categories, synthesizing an unpersisted (headRank=null)
     * placeholder for any criteria that doesn't have a saved result row yet -- otherwise the head
     * would see nothing to rate until they'd already rated something.
     */
    public List<AnnualEvaluationCriteriaResult> getCriteriaResults(Long evaluationId) {
        AnnualEvaluation evaluation = requireEvaluation(evaluationId);
        List<AnnualEvaluationCriteriaResult> existing = criteriaResultRepository.findByEvaluationId(evaluationId);
        List<AnnualEvaluationCriteriaResult> results = new java.util.ArrayList<>();
        for (AnnualEvaluationCategoryResult categoryResult : categoryResultRepository.findByEvaluationId(evaluationId)) {
            for (CategoryCriteria criteria : criteriaRepository.findByCategoryIdOrderBySortOrder(categoryResult.getCategory().getId())) {
                results.add(existing.stream()
                        .filter(r -> r.getCriteria().getId().equals(criteria.getId()))
                        .findFirst()
                        .orElseGet(() -> AnnualEvaluationCriteriaResult.builder()
                                .evaluation(evaluation).criteria(criteria).build()));
            }
        }
        return results;
    }

    /** Goals from the employee's deployed goal cycle for this evaluation's year, if any (seeded at creation time -- see {@link #createEvaluation}). */
    public List<AnnualEvaluationGoalResult> getGoalResults(Long evaluationId) {
        return goalResultRepository.findByEvaluationId(evaluationId);
    }

    /** Achievements logged within this evaluation's academic year -- what the employee tags to criteria. */
    public List<PortfolioEntry> getEntriesInPeriod(Long evaluationId, Long currentUserId) {
        AnnualEvaluation evaluation = requireEvaluation(evaluationId);
        assertCanView(evaluation.getEmployee(), evaluation.getHead(), currentUserId);
        return entriesInPeriod(evaluation);
    }

    // ─── Employee stage (DRAFT only) ───────────────────────────────────────────────────

    public PortfolioEntry updateEntryDesignation(Long evaluationId, Long entryId, Long categoryId, Long criteriaId, Long goalId, Long currentUserId) {
        AnnualEvaluation evaluation = requireEvaluation(evaluationId);
        assertIsEmployee(evaluation, currentUserId);
        assertState(evaluation, "Achievement designations can only be edited while the evaluation is in DRAFT",
                AnnualEvaluationState.DRAFT, AnnualEvaluationState.RETURNED_TO_EMPLOYEE);

        PortfolioEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("PortfolioEntry", entryId));
        if (!entry.getEmployee().getId().equals(evaluation.getEmployee().getId())) {
            throw new UnauthorizedException("Entry does not belong to this evaluation's employee");
        }
        // An achievement-recording tool (e.g. Teaching Evaluations) fixes its own category/criteria
        // at creation time -- that pairing must stay locked afterward too, even though the goal
        // link is still freely reassignable like any other achievement.
        if (entry.getAchievement().getCreatedByModuleCode() != null) {
            Long currentCriteriaId = entry.getCriteria() != null ? entry.getCriteria().getId() : null;
            if (!entry.getCategory().getId().equals(categoryId) || !java.util.Objects.equals(currentCriteriaId, criteriaId)) {
                throw new BusinessRuleException(
                        "This achievement was generated by an achievement-recording tool -- its category and criterion are fixed. You can still change its related goal.");
            }
        }

        PortfolioCategory category = requireApplicableCategory(evaluation, categoryId);
        CategoryCriteria criteria = null;
        if (criteriaId != null) {
            criteria = criteriaRepository.findById(criteriaId)
                    .orElseThrow(() -> new ResourceNotFoundException("CategoryCriteria", criteriaId));
            if (!criteria.getCategory().getId().equals(category.getId())) {
                throw new BusinessRuleException("Criteria does not belong to the selected category");
            }
        }

        EmployeeGoal goal = null;
        if (goalId != null) {
            goal = goalRepository.findById(goalId)
                    .orElseThrow(() -> new ResourceNotFoundException("EmployeeGoal", goalId));
            if (!goal.getCycle().getEmployee().getId().equals(evaluation.getEmployee().getId())) {
                throw new BusinessRuleException("Goal does not belong to this evaluation's employee");
            }
        }

        entry.setCategory(category);
        entry.setCriteria(criteria);
        entry.setGoal(goal);
        PortfolioEntry saved = entryRepository.save(entry);

        // Tagging something to a criteria/goal previously marked "nothing to report" resolves that
        // contradiction automatically rather than leaving both true at once.
        if (criteria != null) {
            criteriaResultRepository.findByEvaluationIdAndCriteriaId(evaluationId, criteria.getId())
                    .filter(AnnualEvaluationCriteriaResult::getEmployeeNothingToReport)
                    .ifPresent(r -> { r.setEmployeeNothingToReport(false); criteriaResultRepository.save(r); });
        }
        if (goal != null) {
            goalResultRepository.findByEvaluationIdAndGoalId(evaluationId, goal.getId())
                    .filter(AnnualEvaluationGoalResult::getEmployeeNothingToReport)
                    .ifPresent(r -> { r.setEmployeeNothingToReport(false); goalResultRepository.save(r); });
        }
        return saved;
    }

    /**
     * Same contradiction-resolution as above ("nothing to report" no longer holds once an
     * achievement is tagged), but for callers that don't already have the evaluationId in hand --
     * PortfolioEntryService logs/updates achievements with a criteria/goal already set at that
     * point, rather than going through {@link #updateEntryDesignation}. Resolves the employee's
     * evaluation for the achievement's own academic year (matched by assessment period name, same
     * as {@link PortfolioEntry#belongsToAcademicYear}); no-ops if no matching year or no
     * evaluation exists yet for it -- there's nothing to clear either way.
     */
    public void clearNothingToReportContradiction(Long employeeId, AssessmentPeriod assessmentPeriod, Long criteriaId, Long goalId) {
        if (assessmentPeriod == null) {
            return;
        }
        academicYearRepository.findByName(assessmentPeriod.getName()).ifPresent(year ->
                evaluationRepository.findByEmployeeIdAndAcademicYearId(employeeId, year.getId()).ifPresent(evaluation -> {
                    if (criteriaId != null) {
                        criteriaResultRepository.findByEvaluationIdAndCriteriaId(evaluation.getId(), criteriaId)
                                .filter(AnnualEvaluationCriteriaResult::getEmployeeNothingToReport)
                                .ifPresent(r -> { r.setEmployeeNothingToReport(false); criteriaResultRepository.save(r); });
                    }
                    if (goalId != null) {
                        goalResultRepository.findByEvaluationIdAndGoalId(evaluation.getId(), goalId)
                                .filter(AnnualEvaluationGoalResult::getEmployeeNothingToReport)
                                .ifPresent(r -> { r.setEmployeeNothingToReport(false); goalResultRepository.save(r); });
                    }
                }));
    }

    public AnnualEvaluationCategoryResult updateSelfRank(Long evaluationId, Long categoryId, Integer rank, Long currentUserId) {
        AnnualEvaluation evaluation = requireEvaluation(evaluationId);
        assertIsEmployee(evaluation, currentUserId);
        assertState(evaluation, "Self-assessment can only be edited while the evaluation is in DRAFT",
                AnnualEvaluationState.DRAFT, AnnualEvaluationState.RETURNED_TO_EMPLOYEE);
        assertValidRank(rank);

        AnnualEvaluationCategoryResult result = requireCategoryResult(evaluation, categoryId);
        result.setEmployeeSelfRank(rank);
        return categoryResultRepository.save(result);
    }

    /** One self-rank covers the whole Annual Goals section -- parallel to how each category has its own single self-rank. */
    public AnnualEvaluation updateGoalsSelfRank(Long evaluationId, Integer rank, Long currentUserId) {
        AnnualEvaluation evaluation = requireEvaluation(evaluationId);
        assertIsEmployee(evaluation, currentUserId);
        assertState(evaluation, "Self-assessment can only be edited while the evaluation is in DRAFT",
                AnnualEvaluationState.DRAFT, AnnualEvaluationState.RETURNED_TO_EMPLOYEE);
        assertValidRank(rank);

        evaluation.setGoalsEmployeeSelfRank(rank);
        return evaluationRepository.save(evaluation);
    }

    /** Declares no achievements apply to this criteria this cycle. Rejected if an entry already references it -- remove the tag first. */
    public AnnualEvaluationCriteriaResult markCriteriaNothingToReport(Long evaluationId, Long criteriaId, boolean nothingToReport, Long currentUserId) {
        AnnualEvaluation evaluation = requireEvaluation(evaluationId);
        assertIsEmployee(evaluation, currentUserId);
        assertState(evaluation, "Self-assessment can only be edited while the evaluation is in DRAFT",
                AnnualEvaluationState.DRAFT, AnnualEvaluationState.RETURNED_TO_EMPLOYEE);

        CategoryCriteria criteria = criteriaRepository.findById(criteriaId)
                .orElseThrow(() -> new ResourceNotFoundException("CategoryCriteria", criteriaId));
        if (nothingToReport) {
            boolean hasEntries = entriesInPeriod(evaluation).stream()
                    .anyMatch(e -> e.getCriteria() != null && e.getCriteria().getId().equals(criteriaId));
            if (hasEntries) {
                throw new BusinessRuleException("Remove the achievement(s) tagged to this criteria before marking it as nothing to report");
            }
        }

        AnnualEvaluationCriteriaResult result = criteriaResultRepository.findByEvaluationIdAndCriteriaId(evaluationId, criteriaId)
                .orElseGet(() -> AnnualEvaluationCriteriaResult.builder().evaluation(evaluation).criteria(criteria).build());
        result.setEmployeeNothingToReport(nothingToReport);
        return criteriaResultRepository.save(result);
    }

    /** Declares no progress to report for this goal this cycle. Rejected if an entry already references it -- remove the tag first. */
    public AnnualEvaluationGoalResult markGoalNothingToReport(Long evaluationId, Long goalId, boolean nothingToReport, Long currentUserId) {
        AnnualEvaluation evaluation = requireEvaluation(evaluationId);
        assertIsEmployee(evaluation, currentUserId);
        assertState(evaluation, "Self-assessment can only be edited while the evaluation is in DRAFT",
                AnnualEvaluationState.DRAFT, AnnualEvaluationState.RETURNED_TO_EMPLOYEE);

        if (nothingToReport) {
            boolean hasEntries = entriesInPeriod(evaluation).stream()
                    .anyMatch(e -> e.getGoal() != null && e.getGoal().getId().equals(goalId));
            if (hasEntries) {
                throw new BusinessRuleException("Remove the achievement(s) tagged to this goal before marking it as nothing to report");
            }
        }

        AnnualEvaluationGoalResult result = goalResultRepository.findByEvaluationIdAndGoalId(evaluationId, goalId)
                .orElseThrow(() -> new ResourceNotFoundException("AnnualEvaluationGoalResult", goalId));
        result.setEmployeeNothingToReport(nothingToReport);
        return goalResultRepository.save(result);
    }

    public AnnualEvaluation submitEmployeeSelfAssessment(Long evaluationId, Long currentUserId) {
        AnnualEvaluation evaluation = requireEvaluation(evaluationId);
        assertIsEmployee(evaluation, currentUserId);
        assertState(evaluation, "This evaluation has already been submitted",
                AnnualEvaluationState.DRAFT, AnnualEvaluationState.RETURNED_TO_EMPLOYEE);

        List<AnnualEvaluationCategoryResult> results = categoryResultRepository.findByEvaluationId(evaluationId);
        boolean missingSelfRank = results.stream().anyMatch(r -> r.getEmployeeSelfRank() == null);
        if (missingSelfRank) {
            throw new BusinessRuleException("Every category must have a self-assessment rank before submitting");
        }

        List<PortfolioEntry> entries = entriesInPeriod(evaluation);
        boolean missingCriteria = entries.stream().anyMatch(e -> e.getCriteria() == null);
        if (missingCriteria) {
            throw new BusinessRuleException("Every achievement logged this academic year must be tagged to a criteria before submitting");
        }

        boolean everyCriteriaCovered = getCriteriaResults(evaluationId).stream()
                .allMatch(r -> Boolean.TRUE.equals(r.getEmployeeNothingToReport())
                        || entries.stream().anyMatch(e -> e.getCriteria() != null && e.getCriteria().getId().equals(r.getCriteria().getId())));
        if (!everyCriteriaCovered) {
            throw new BusinessRuleException("Every criteria must have either an achievement or be marked 'nothing to report' before submitting");
        }

        for (AnnualEvaluationCriteriaResult cr : getCriteriaResults(evaluationId)) {
            for (CriteriaAchievementModule module : portfolioCategoryService.getAchievementModulesForCriteria(cr.getCriteria().getId())) {
                if (!Boolean.TRUE.equals(module.getMandatory())) {
                    continue;
                }
                boolean hasOne = entries.stream().anyMatch(e -> e.getCriteria() != null
                        && e.getCriteria().getId().equals(cr.getCriteria().getId())
                        && module.getModuleCode().equals(e.getAchievement().getCreatedByModuleCode()));
                if (!hasOne) {
                    throw new BusinessRuleException("At least one achievement recorded through the "
                            + achievementModuleDisplayName(module.getModuleCode()) + " tool is required for "
                            + cr.getCriteria().getCriteriaName() + " before submitting");
                }
            }
        }

        List<AnnualEvaluationGoalResult> goalResults = goalResultRepository.findByEvaluationId(evaluationId);
        boolean everyGoalCovered = goalResults.stream()
                .allMatch(r -> Boolean.TRUE.equals(r.getEmployeeNothingToReport())
                        || entries.stream().anyMatch(e -> e.getGoal() != null && e.getGoal().getId().equals(r.getGoal().getId())));
        if (!everyGoalCovered) {
            throw new BusinessRuleException("Every goal must have either an achievement or be marked 'nothing to report' before submitting");
        }

        if (!goalResults.isEmpty() && evaluation.getGoalsEmployeeSelfRank() == null) {
            throw new BusinessRuleException("A self-assessment rank for the Annual Goals section is required before submitting");
        }

        if (!goalResults.isEmpty() && isBlank(evaluation.getGoalsEmployeeComments())) {
            throw new BusinessRuleException("Comments and reflection for the Annual Goals section are required before submitting");
        }

        if (results.stream().anyMatch(r -> isBlank(r.getEmployeeComments()))) {
            throw new BusinessRuleException("Every category must have your comments and reflection before submitting");
        }

        if (isBlank(evaluation.getEmployeeFinalSummary())) {
            throw new BusinessRuleException("A General Final Summary Statement is required before submitting");
        }

        evaluation.setEmployeeSubmittedAt(LocalDateTime.now());
        evaluation.setState(AnnualEvaluationState.EMPLOYEE_SUBMITTED);
        AnnualEvaluation saved = evaluationRepository.save(evaluation);

        auditService.log(evaluation.getEmployee(), "SUBMIT_ANNUAL_SELF_ASSESSMENT", "AnnualEvaluation", evaluationId,
                null, "Submitted annual self-assessment");
        eventPublisher.publishEvent(new AnnualEvaluationSubmittedEvent(evaluationId));
        return saved;
    }

    private List<PortfolioEntry> entriesInPeriod(AnnualEvaluation evaluation) {
        AcademicYear year = evaluation.getAcademicYear();
        return entryRepository.findByEmployeeIdOrderByCreatedAtDesc(evaluation.getEmployee().getId()).stream()
                .filter(e -> e.belongsToAcademicYear(year))
                .toList();
    }

    // ─── Head stage (from EMPLOYEE_SUBMITTED onward, until locked) ─────────────────────

    public AnnualEvaluationCriteriaResult updateCriteriaRank(Long evaluationId, Long criteriaId, Integer rank, Long currentUserId) {
        AnnualEvaluation evaluation = requireEvaluation(evaluationId);
        assertIsHead(evaluation, currentUserId);
        assertHeadCanEdit(evaluation);
        assertValidRank(rank);

        CategoryCriteria criteria = criteriaRepository.findById(criteriaId)
                .orElseThrow(() -> new ResourceNotFoundException("CategoryCriteria", criteriaId));
        requireApplicableCategory(evaluation, criteria.getCategory().getId());

        AnnualEvaluationCriteriaResult result = criteriaResultRepository.findByEvaluationIdAndCriteriaId(evaluationId, criteriaId)
                .orElseGet(() -> AnnualEvaluationCriteriaResult.builder().evaluation(evaluation).criteria(criteria).build());
        result.setHeadRank(rank);
        AnnualEvaluationCriteriaResult saved = criteriaResultRepository.save(result);

        notifyIfEditedAfterSubmit(evaluation);
        return saved;
    }

    public AnnualEvaluationCategoryResult updateCategoryHeadRank(Long evaluationId, Long categoryId, Integer rank, Long currentUserId) {
        AnnualEvaluation evaluation = requireEvaluation(evaluationId);
        assertIsHead(evaluation, currentUserId);
        assertHeadCanEdit(evaluation);
        assertValidRank(rank);

        AnnualEvaluationCategoryResult result = requireCategoryResult(evaluation, categoryId);
        result.setHeadCategoryRank(rank);
        AnnualEvaluationCategoryResult saved = categoryResultRepository.save(result);

        notifyIfEditedAfterSubmit(evaluation);
        return saved;
    }

    /** The head's written remarks for a category -- separate from the numeric rank, required before submitting. */
    public AnnualEvaluationCategoryResult updateCategoryHeadComments(Long evaluationId, Long categoryId,
                                                                      String strengths, String improvements, Long currentUserId) {
        AnnualEvaluation evaluation = requireEvaluation(evaluationId);
        assertIsHead(evaluation, currentUserId);
        assertHeadCanEdit(evaluation);

        AnnualEvaluationCategoryResult result = requireCategoryResult(evaluation, categoryId);
        result.setHeadCommentsStrengths(strengths);
        result.setHeadCommentsImprovements(improvements);
        AnnualEvaluationCategoryResult saved = categoryResultRepository.save(result);

        notifyIfEditedAfterSubmit(evaluation);
        return saved;
    }

    public AnnualEvaluationGoalResult updateGoalHeadRank(Long evaluationId, Long goalId, Integer rank, Long currentUserId) {
        AnnualEvaluation evaluation = requireEvaluation(evaluationId);
        assertIsHead(evaluation, currentUserId);
        assertHeadCanEdit(evaluation);
        assertValidRank(rank);

        AnnualEvaluationGoalResult result = goalResultRepository.findByEvaluationIdAndGoalId(evaluationId, goalId)
                .orElseThrow(() -> new ResourceNotFoundException("AnnualEvaluationGoalResult", goalId));
        result.setHeadGoalRank(rank);
        AnnualEvaluationGoalResult saved = goalResultRepository.save(result);

        notifyIfEditedAfterSubmit(evaluation);
        return saved;
    }

    /** The head's written remarks for the whole Annual Goals section -- one field, required before submitting, same as a category's. */
    public AnnualEvaluation updateGoalsHeadComments(Long evaluationId, String strengths, String improvements, Long currentUserId) {
        AnnualEvaluation evaluation = requireEvaluation(evaluationId);
        assertIsHead(evaluation, currentUserId);
        assertHeadCanEdit(evaluation);

        evaluation.setGoalsHeadCommentsStrengths(strengths);
        evaluation.setGoalsHeadCommentsImprovements(improvements);
        AnnualEvaluation saved = evaluationRepository.save(evaluation);

        notifyIfEditedAfterSubmit(evaluation);
        return saved;
    }

    public AnnualEvaluation updateGoalsHeadRank(Long evaluationId, Integer rank, Long currentUserId) {
        AnnualEvaluation evaluation = requireEvaluation(evaluationId);
        assertIsHead(evaluation, currentUserId);
        assertHeadCanEdit(evaluation);
        assertValidRank(rank);

        evaluation.setGoalsHeadRank(rank);
        AnnualEvaluation saved = evaluationRepository.save(evaluation);

        notifyIfEditedAfterSubmit(evaluation);
        return saved;
    }

    public AnnualEvaluation updateOverallRank(Long evaluationId, Integer rank, Long currentUserId) {
        AnnualEvaluation evaluation = requireEvaluation(evaluationId);
        assertIsHead(evaluation, currentUserId);
        assertHeadCanEdit(evaluation);
        assertValidRank(rank);

        evaluation.setHeadOverallRank(rank);
        AnnualEvaluation saved = evaluationRepository.save(evaluation);

        notifyIfEditedAfterSubmit(evaluation);
        return saved;
    }

    private void notifyIfEditedAfterSubmit(AnnualEvaluation evaluation) {
        if (evaluation.getState() == AnnualEvaluationState.HEAD_SUBMITTED) {
            eventPublisher.publishEvent(new AnnualEvaluationEditedEvent(evaluation.getId()));
        }
    }

    /**
     * The head's full completeness gate -- every category/criteria/goal rank and comment, the
     * overall rank, and every Next Cycle Goal reviewed. Shared by {@link
     * #submitAndSignHeadEvaluation} (this is what "submit" means now that submitting and signing
     * are one action).
     */
    private void assertHeadEvaluationComplete(Long evaluationId, AnnualEvaluation evaluation) {
        List<AnnualEvaluationCategoryResult> categoryResults = categoryResultRepository.findByEvaluationId(evaluationId);
        if (categoryResults.stream().anyMatch(r -> r.getHeadCategoryRank() == null)) {
            throw new BusinessRuleException("Every category must have a head rank before submitting");
        }
        if (categoryResults.stream().anyMatch(r -> isBlank(r.getHeadCommentsStrengths()) || isBlank(r.getHeadCommentsImprovements()))) {
            throw new BusinessRuleException("Every category must have both Strengths and Potential Improvements comments before submitting");
        }
        List<CategoryCriteria> allCriteria = categoryResults.stream()
                .flatMap(r -> criteriaRepository.findByCategoryIdOrderBySortOrder(r.getCategory().getId()).stream())
                .toList();
        List<AnnualEvaluationCriteriaResult> criteriaResults = criteriaResultRepository.findByEvaluationId(evaluationId);
        boolean allCriteriaRanked = allCriteria.stream().allMatch(c -> criteriaResults.stream()
                .anyMatch(r -> r.getCriteria().getId().equals(c.getId()) && r.getHeadRank() != null));
        if (!allCriteriaRanked) {
            throw new BusinessRuleException("Every criteria must have a head rank before submitting");
        }
        List<AnnualEvaluationGoalResult> goalResults = goalResultRepository.findByEvaluationId(evaluationId);
        if (goalResults.stream().anyMatch(r -> r.getHeadGoalRank() == null)) {
            throw new BusinessRuleException("Every goal must have a head rank before submitting");
        }
        if (!goalResults.isEmpty() && evaluation.getGoalsHeadRank() == null) {
            throw new BusinessRuleException("The Annual Goals section must have a head rank before submitting");
        }
        if (!goalResults.isEmpty() && (isBlank(evaluation.getGoalsHeadCommentsStrengths()) || isBlank(evaluation.getGoalsHeadCommentsImprovements()))) {
            throw new BusinessRuleException("Both Strengths and Potential Improvements comments for the Annual Goals section are required before submitting");
        }
        if (evaluation.getHeadOverallRank() == null) {
            throw new BusinessRuleException("An overall annual performance rank is required before submitting");
        }
        List<AnnualEvaluationNextCycleGoal> nextCycleGoals = nextCycleGoalRepository.findByEvaluationIdOrderBySortOrder(evaluationId);
        if (nextCycleGoals.isEmpty()) {
            throw new BusinessRuleException("At least one Next Cycle Goal is required before submitting");
        }
        if (nextCycleGoals.stream().anyMatch(g -> g.getLeaderActionType() == null)) {
            throw new BusinessRuleException("Every Next Cycle Goal must be reviewed before submitting");
        }
    }

    /**
     * Combines what used to be two separate head actions (submit, then sign) into one -- the head
     * types their name and the evaluation goes straight to the employee's sign/refuse step.
     */
    public AnnualEvaluation submitAndSignHeadEvaluation(Long evaluationId, String signatureName, Long currentUserId) {
        AnnualEvaluation evaluation = requireEvaluation(evaluationId);
        assertIsHead(evaluation, currentUserId);
        assertState(evaluation, AnnualEvaluationState.EMPLOYEE_SUBMITTED,
                "The head's evaluation can only be submitted once, after the employee's self-assessment");
        if (signatureName == null || signatureName.isBlank()) {
            throw new BusinessRuleException("Type your full name to sign this evaluation");
        }
        assertHeadEvaluationComplete(evaluationId, evaluation);

        LocalDateTime now = LocalDateTime.now();
        evaluation.setHeadSubmittedAt(now);
        evaluation.setHeadSignedAt(now);
        evaluation.setHeadSignatureName(signatureName);
        evaluation.setState(AnnualEvaluationState.HEAD_SUBMITTED);
        AnnualEvaluation saved = evaluationRepository.save(evaluation);

        auditService.log(evaluation.getHead(), "SUBMIT_AND_SIGN_ANNUAL_HEAD_EVALUATION", "AnnualEvaluation", evaluationId,
                null, "Signed and submitted head evaluation; ready for employee signature");
        eventPublisher.publishEvent(new AnnualEvaluationHeadReadyEvent(evaluationId));
        eventPublisher.publishEvent(new AnnualEvaluationSignedEvent(evaluationId, true, false));
        return saved;
    }

    /**
     * Sends a fully or partially-rated evaluation back to the employee for one more round -- e.g.
     * to add a missed achievement or fix something seriously incomplete -- before the head does
     * their final pass and submits-and-signs. Deliberately has no completeness gate. Usable exactly
     * once per evaluation: {@link AnnualEvaluation#getReturnedToEmployeeAt()} being non-null is what
     * hides the button afterward.
     */
    public AnnualEvaluation returnToEmployeeForReview(Long evaluationId, Long currentUserId) {
        AnnualEvaluation evaluation = requireEvaluation(evaluationId);
        assertIsHead(evaluation, currentUserId);
        assertState(evaluation, AnnualEvaluationState.EMPLOYEE_SUBMITTED,
                "This evaluation cannot be returned to the employee right now");
        if (evaluation.getReturnedToEmployeeAt() != null) {
            throw new BusinessRuleException("This evaluation has already been returned to the employee once");
        }

        evaluation.setReturnedToEmployeeAt(LocalDateTime.now());
        evaluation.setState(AnnualEvaluationState.RETURNED_TO_EMPLOYEE);
        AnnualEvaluation saved = evaluationRepository.save(evaluation);

        auditService.log(evaluation.getHead(), "RETURN_ANNUAL_EVALUATION_TO_EMPLOYEE", "AnnualEvaluation", evaluationId,
                null, "Returned evaluation to employee for review and update");
        eventPublisher.publishEvent(new AnnualEvaluationReturnedToEmployeeEvent(evaluationId));
        return saved;
    }

    /** The employee's own reflection for a category -- optional, editable in DRAFT and RETURNED_TO_EMPLOYEE, shown before the head's comments. */
    public AnnualEvaluationCategoryResult updateCategoryEmployeeComments(Long evaluationId, Long categoryId, String comments, Long currentUserId) {
        AnnualEvaluation evaluation = requireEvaluation(evaluationId);
        assertIsEmployee(evaluation, currentUserId);
        assertState(evaluation, "Your comments can only be edited while the evaluation is in DRAFT or returned to you for review",
                AnnualEvaluationState.DRAFT, AnnualEvaluationState.RETURNED_TO_EMPLOYEE);

        AnnualEvaluationCategoryResult result = requireCategoryResult(evaluation, categoryId);
        result.setEmployeeComments(comments);
        return categoryResultRepository.save(result);
    }

    /** The employee's own reflection for a criterion -- optional, editable in DRAFT and RETURNED_TO_EMPLOYEE, parallel to the per-category one. */
    public AnnualEvaluationCriteriaResult updateCriteriaEmployeeComments(Long evaluationId, Long criteriaId, String comments, Long currentUserId) {
        AnnualEvaluation evaluation = requireEvaluation(evaluationId);
        assertIsEmployee(evaluation, currentUserId);
        assertState(evaluation, "Your comments can only be edited while the evaluation is in DRAFT or returned to you for review",
                AnnualEvaluationState.DRAFT, AnnualEvaluationState.RETURNED_TO_EMPLOYEE);

        CategoryCriteria criteria = criteriaRepository.findById(criteriaId)
                .orElseThrow(() -> new ResourceNotFoundException("CategoryCriteria", criteriaId));

        AnnualEvaluationCriteriaResult result = criteriaResultRepository.findByEvaluationIdAndCriteriaId(evaluationId, criteriaId)
                .orElseGet(() -> AnnualEvaluationCriteriaResult.builder().evaluation(evaluation).criteria(criteria).build());
        result.setEmployeeComments(comments);
        return criteriaResultRepository.save(result);
    }

    /** The employee's own reflection for the whole Annual Goals section -- one field, parallel to the per-category one. */
    public AnnualEvaluation updateGoalsEmployeeComments(Long evaluationId, String comments, Long currentUserId) {
        AnnualEvaluation evaluation = requireEvaluation(evaluationId);
        assertIsEmployee(evaluation, currentUserId);
        assertState(evaluation, "Your comments can only be edited while the evaluation is in DRAFT or returned to you for review",
                AnnualEvaluationState.DRAFT, AnnualEvaluationState.RETURNED_TO_EMPLOYEE);

        evaluation.setGoalsEmployeeComments(comments);
        return evaluationRepository.save(evaluation);
    }

    /** The employee's required closing statement for the whole evaluation -- distinct from the per-category/goals reflections. */
    public AnnualEvaluation updateEmployeeFinalSummary(Long evaluationId, String comments, Long currentUserId) {
        AnnualEvaluation evaluation = requireEvaluation(evaluationId);
        assertIsEmployee(evaluation, currentUserId);
        assertState(evaluation, "Your comments can only be edited while the evaluation is in DRAFT or returned to you for review",
                AnnualEvaluationState.DRAFT, AnnualEvaluationState.RETURNED_TO_EMPLOYEE);

        evaluation.setEmployeeFinalSummary(comments);
        return evaluationRepository.save(evaluation);
    }

    // ─── Signatures ─────────────────────────────────────────────────────────────────────

    public AnnualEvaluation signAsEmployee(Long evaluationId, String signatureName, Long currentUserId) {
        AnnualEvaluation evaluation = requireEvaluation(evaluationId);
        assertIsEmployee(evaluation, currentUserId);
        assertState(evaluation, AnnualEvaluationState.HEAD_SUBMITTED, "The evaluation is not ready for signature yet");
        assertEmployeeHasNotActed(evaluation);
        assertAllNextCycleGoalsReviewedByEmployee(evaluationId);
        if (signatureName == null || signatureName.isBlank()) {
            throw new BusinessRuleException("Type your full name to sign this evaluation");
        }

        evaluation.setEmployeeSignedAt(LocalDateTime.now());
        evaluation.setEmployeeSignatureName(signatureName);
        concludeIfBothDone(evaluation);
        AnnualEvaluation saved = evaluationRepository.save(evaluation);

        auditService.log(evaluation.getEmployee(), "SIGN_ANNUAL_EVALUATION", "AnnualEvaluation", evaluationId, null, "Employee signed");
        eventPublisher.publishEvent(new AnnualEvaluationSignedEvent(evaluationId, false, false));
        return saved;
    }

    public AnnualEvaluation refuseToSign(Long evaluationId, String rationale, Long currentUserId) {
        AnnualEvaluation evaluation = requireEvaluation(evaluationId);
        assertIsEmployee(evaluation, currentUserId);
        assertState(evaluation, AnnualEvaluationState.HEAD_SUBMITTED, "The evaluation is not ready for signature yet");
        assertEmployeeHasNotActed(evaluation);
        assertAllNextCycleGoalsReviewedByEmployee(evaluationId);
        if (rationale == null || rationale.isBlank()) {
            throw new BusinessRuleException("A rationale is required to refuse to sign");
        }

        evaluation.setEmployeeRefused(true);
        evaluation.setEmployeeRefusalRationale(rationale);
        concludeIfBothDone(evaluation);
        AnnualEvaluation saved = evaluationRepository.save(evaluation);

        auditService.log(evaluation.getEmployee(), "REFUSE_SIGN_ANNUAL_EVALUATION", "AnnualEvaluation", evaluationId, null, rationale);
        eventPublisher.publishEvent(new AnnualEvaluationSignedEvent(evaluationId, false, true));
        return saved;
    }

    private void assertEmployeeHasNotActed(AnnualEvaluation evaluation) {
        if (evaluation.getEmployeeSignedAt() != null || Boolean.TRUE.equals(evaluation.getEmployeeRefused())) {
            throw new BusinessRuleException("You have already signed or refused to sign this evaluation");
        }
    }

    /** The employee must review every Next Cycle Goal (approve/edit/reject) before they may sign or refuse. */
    private void assertAllNextCycleGoalsReviewedByEmployee(Long evaluationId) {
        List<AnnualEvaluationNextCycleGoal> nextCycleGoals = nextCycleGoalRepository.findByEvaluationIdOrderBySortOrder(evaluationId);
        if (nextCycleGoals.stream().anyMatch(g -> g.getEmployeeActionType() == null)) {
            throw new BusinessRuleException("You must review every Next Cycle Goal before signing or refusing this evaluation");
        }
    }

    private void concludeIfBothDone(AnnualEvaluation evaluation) {
        if (evaluation.isConcluded()) {
            evaluation.setState(AnnualEvaluationState.CONCLUDED);
        }
    }

    // ─── Shared guards ──────────────────────────────────────────────────────────────────

    private AnnualEvaluation requireEvaluation(Long evaluationId) {
        return evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new ResourceNotFoundException("AnnualEvaluation", evaluationId));
    }

    private AnnualEvaluationCategoryResult requireCategoryResult(AnnualEvaluation evaluation, Long categoryId) {
        return categoryResultRepository.findByEvaluationIdAndCategoryId(evaluation.getId(), categoryId)
                .orElseThrow(() -> new BusinessRuleException("Category does not apply to this evaluation"));
    }

    private PortfolioCategory requireApplicableCategory(AnnualEvaluation evaluation, Long categoryId) {
        AnnualEvaluationCategoryResult result = requireCategoryResult(evaluation, categoryId);
        return result.getCategory();
    }

    private void assertValidRank(Integer rank) {
        if (rank == null || rank < 1 || rank > 5) {
            throw new BusinessRuleException("Rank must be between 1 and 5");
        }
    }

    private void assertState(AnnualEvaluation evaluation, AnnualEvaluationState expected, String message) {
        if (evaluation.getState() != expected) {
            throw new BusinessRuleException(message);
        }
    }

    private void assertState(AnnualEvaluation evaluation, String message, AnnualEvaluationState... allowed) {
        for (AnnualEvaluationState state : allowed) {
            if (evaluation.getState() == state) {
                return;
            }
        }
        throw new BusinessRuleException(message);
    }

    private void assertIsEmployee(AnnualEvaluation evaluation, Long currentUserId) {
        if (!evaluation.getEmployee().getId().equals(currentUserId)) {
            throw new UnauthorizedException("Only the employee this evaluation belongs to can perform this action");
        }
    }

    private void assertIsHead(AnnualEvaluation evaluation, Long currentUserId) {
        if (!evaluation.getHead().getId().equals(currentUserId)) {
            throw new UnauthorizedException("Only this employee's head can perform this action");
        }
    }

    private void assertHeadCanEdit(AnnualEvaluation evaluation) {
        if (evaluation.getState() != AnnualEvaluationState.EMPLOYEE_SUBMITTED
                && evaluation.getState() != AnnualEvaluationState.HEAD_SUBMITTED) {
            throw new BusinessRuleException("The head cannot rate this evaluation yet -- the employee must submit their self-assessment first");
        }
        if (evaluation.isLocked()) {
            throw new BusinessRuleException("This evaluation is locked -- a signature has already been recorded");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private String achievementModuleDisplayName(String moduleCode) {
        return portfolioCategoryService.listAchievementModules().stream()
                .filter(m -> m.getCode().equals(moduleCode))
                .findFirst()
                .map(CustomizableAchievementModule::getDisplayName)
                .orElse(moduleCode);
    }

    private void assertCanView(AppUser employee, AppUser head, Long currentUserId) {
        boolean allowed = employee.getId().equals(currentUserId) || head.getId().equals(currentUserId);
        if (!allowed) {
            AppUser currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));
            allowed = currentUser.hasRole(com.rit.spms.domain.enums.SystemRole.ADMIN);
        }
        // A head further up the org hierarchy (e.g. a Dean over several departments) gets read-only
        // visibility into every evaluation beneath them, even ones where they aren't the literal rater.
        if (!allowed && employee.getDepartment() != null) {
            allowed = permissionService.resolveHierarchyDepartmentIds(currentUserId).contains(employee.getDepartment().getId());
        }
        if (!allowed) {
            throw new UnauthorizedException("Only the employee, their head, an admin, or a head in their org hierarchy can view this evaluation");
        }
    }
}
