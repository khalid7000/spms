package com.rit.spms.service;

import com.rit.spms.domain.*;
import com.rit.spms.domain.enums.AnnualEvaluationState;
import com.rit.spms.domain.enums.PortfolioReviewActionType;
import com.rit.spms.domain.enums.SystemRole;
import com.rit.spms.exception.BusinessRuleException;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.exception.UnauthorizedException;
import com.rit.spms.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Goals for the employee's NEXT annual cycle, drafted and reviewed by both the head and the
 * employee during THIS Annual Evaluation's own review/sign exchange (not a separate approval
 * workflow) -- see AnnualEvaluation's nextCycle* fields and AnnualEvaluationNextCycleGoal. Mirrors
 * EmployeeGoalSuggestionService's AI-generation mechanics exactly, reusing the same
 * PortfolioGoalSuggestionGenerator. Once the evaluation concludes, whichever goals neither party
 * rejected are eligible for reuse from Team Goal Setting (EmployeeGoalCycleService).
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AnnualEvaluationNextCycleGoalService {

    private final AnnualEvaluationRepository evaluationRepository;
    private final AnnualEvaluationNextCycleGoalRepository nextCycleGoalRepository;
    private final PortfolioCategoryRepository categoryRepository;
    private final PortfolioCategoryService portfolioCategoryService;
    private final PortfolioGoalSuggestionGenerator goalGenerator;
    private final AppUserRepository userRepository;

    // ─── Notes + AI generation ──────────────────────────────────────────────

    public AnnualEvaluation updateNotes(Long evaluationId, String strengths, String weaknesses, Long currentUserId) {
        AnnualEvaluation evaluation = requireEvaluation(evaluationId);
        assertHeadCanEdit(evaluation, currentUserId);
        evaluation.setNextCycleNotesStrengths(strengths);
        evaluation.setNextCycleNotesWeaknesses(weaknesses);
        return evaluationRepository.save(evaluation);
    }

    /** Returns the employee's available categories, for the controller to hand to generateSuggestionsAsync. */
    public List<PortfolioCategory> assertCanGenerateSuggestions(Long evaluationId, Long currentUserId) {
        AnnualEvaluation evaluation = requireEvaluation(evaluationId);
        assertHeadCanEdit(evaluation, currentUserId);
        return portfolioCategoryService.getCategoriesForUser(evaluation.getEmployee());
    }

    /** Synchronous checkpoint -- see EmployeeGoalSuggestionService.recordGenerationRequested for why this must commit before the @Async call. */
    public void recordGenerationRequested(Long evaluationId) {
        AnnualEvaluation evaluation = requireEvaluation(evaluationId);
        evaluation.setNextCycleGenerationRequestedAt(LocalDateTime.now());
        evaluation.setNextCycleGenerationFailureReason(null);
        evaluationRepository.save(evaluation);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void generateSuggestionsAsync(Long evaluationId, List<PortfolioCategory> availableCategories) {
        AnnualEvaluation evaluation = requireEvaluation(evaluationId);

        Map<String, PortfolioCategory> byName = availableCategories.stream()
                .collect(Collectors.toMap(c -> c.getCategoryName().toLowerCase(), c -> c, (a, b) -> a));

        List<PortfolioGoalSuggestionGenerator.SuggestedGoalDto> suggestions;
        try {
            suggestions = goalGenerator.generateGoalSuggestions(
                    evaluation.getNextCycleNotesStrengths(), evaluation.getNextCycleNotesWeaknesses(), availableCategories);
        } catch (Exception e) {
            log.warn("AI next-cycle-goal generation failed for evaluation {}; left as-is for retry", evaluationId, e);
            evaluation.setNextCycleGenerationFailureReason(failureReason(e));
            evaluationRepository.save(evaluation);
            return;
        }

        nextCycleGoalRepository.deleteByEvaluationId(evaluationId);

        int sortOrder = 0;
        for (PortfolioGoalSuggestionGenerator.SuggestedGoalDto dto : suggestions) {
            PortfolioCategory category = byName.get(dto.categoryName() == null ? "" : dto.categoryName().toLowerCase());
            if (category == null) {
                log.warn("Skipping next-cycle-goal suggestion '{}': category '{}' is not one of this employee's categories",
                        dto.title(), dto.categoryName());
                continue;
            }
            nextCycleGoalRepository.save(AnnualEvaluationNextCycleGoal.builder()
                    .evaluation(evaluation)
                    .category(category)
                    .suggestedTitle(dto.title())
                    .suggestedDescription(dto.description())
                    .rationale(dto.rationale())
                    .rubricUnsatisfactory(dto.rubricUnsatisfactory())
                    .rubricMeetsExpectations(dto.rubricMeetsExpectations())
                    .rubricExceedsExpectations(dto.rubricExceedsExpectations())
                    .generatedByModel(goalGenerator.providerName())
                    .sortOrder(sortOrder++)
                    .build());
        }

        evaluation.setNextCycleGeneratedAt(LocalDateTime.now());
        evaluation.setNextCycleGenerationFailureReason(null);
        evaluationRepository.save(evaluation);
    }

    private static String failureReason(Exception e) {
        String msg = e.getMessage();
        return (msg == null || msg.isBlank()) ? e.getClass().getSimpleName() : msg;
    }

    // ─── CRUD + review ───────────────────────────────────────────────────────

    public List<AnnualEvaluationNextCycleGoal> getGoals(Long evaluationId, Long currentUserId) {
        AnnualEvaluation evaluation = requireEvaluation(evaluationId);
        assertCanView(evaluation, currentUserId);
        return nextCycleGoalRepository.findByEvaluationIdOrderBySortOrder(evaluationId);
    }

    /** "Add a new goal" -- a head-authored entry, pre-approved as-is on the leader side. */
    public AnnualEvaluationNextCycleGoal addGoal(Long evaluationId, Long categoryId, String title, String description,
                                                  String rubricUnsatisfactory, String rubricMeetsExpectations,
                                                  String rubricExceedsExpectations, Long currentUserId) {
        AnnualEvaluation evaluation = requireEvaluation(evaluationId);
        assertHeadCanEdit(evaluation, currentUserId);
        PortfolioCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("PortfolioCategory", categoryId));
        int nextSort = nextCycleGoalRepository.findByEvaluationIdOrderBySortOrder(evaluationId).size();
        return nextCycleGoalRepository.save(AnnualEvaluationNextCycleGoal.builder()
                .evaluation(evaluation).category(category)
                .suggestedTitle(title).suggestedDescription(description)
                .rubricUnsatisfactory(rubricUnsatisfactory)
                .rubricMeetsExpectations(rubricMeetsExpectations)
                .rubricExceedsExpectations(rubricExceedsExpectations)
                .generatedByModel(null)
                .sortOrder(nextSort)
                .leaderActionType(PortfolioReviewActionType.APPROVE_AS_IS)
                .leaderReviewedAt(LocalDateTime.now())
                .build());
    }

    public AnnualEvaluationNextCycleGoal updateRubric(Long evaluationId, Long goalId, String rubricUnsatisfactory,
                                                       String rubricMeetsExpectations, String rubricExceedsExpectations, Long currentUserId) {
        AnnualEvaluation evaluation = requireEvaluation(evaluationId);
        assertHeadCanEdit(evaluation, currentUserId);
        AnnualEvaluationNextCycleGoal goal = requireGoal(evaluationId, goalId);
        goal.setRubricUnsatisfactory(rubricUnsatisfactory);
        goal.setRubricMeetsExpectations(rubricMeetsExpectations);
        goal.setRubricExceedsExpectations(rubricExceedsExpectations);
        return nextCycleGoalRepository.save(goal);
    }

    /** The head's own review -- editable during the head's normal edit window. */
    public AnnualEvaluationNextCycleGoal leaderReview(Long evaluationId, Long goalId, PortfolioReviewActionType actionType,
                                                       String editedTitle, String editedDescription, Long currentUserId) {
        AnnualEvaluation evaluation = requireEvaluation(evaluationId);
        assertHeadCanEdit(evaluation, currentUserId);
        AnnualEvaluationNextCycleGoal goal = requireGoal(evaluationId, goalId);
        goal.setLeaderActionType(actionType);
        goal.setLeaderEditedTitle(editedTitle);
        goal.setLeaderEditedDescription(editedDescription);
        goal.setLeaderReviewedAt(LocalDateTime.now());
        return nextCycleGoalRepository.save(goal);
    }

    /** The employee's own review -- required before they may sign or refuse the evaluation (see AnnualEvaluationService). */
    public AnnualEvaluationNextCycleGoal employeeReview(Long evaluationId, Long goalId, PortfolioReviewActionType actionType,
                                                         String editedTitle, String editedDescription, Long currentUserId) {
        AnnualEvaluation evaluation = requireEvaluation(evaluationId);
        assertEmployeeCanReview(evaluation, currentUserId);
        AnnualEvaluationNextCycleGoal goal = requireGoal(evaluationId, goalId);
        goal.setEmployeeActionType(actionType);
        goal.setEmployeeEditedTitle(editedTitle);
        goal.setEmployeeEditedDescription(editedDescription);
        goal.setEmployeeReviewedAt(LocalDateTime.now());
        return nextCycleGoalRepository.save(goal);
    }

    public void deleteGoal(Long evaluationId, Long goalId, Long currentUserId) {
        AnnualEvaluation evaluation = requireEvaluation(evaluationId);
        assertHeadCanEdit(evaluation, currentUserId);
        AnnualEvaluationNextCycleGoal goal = requireGoal(evaluationId, goalId);
        nextCycleGoalRepository.delete(goal);
    }

    // ─── Guards ───────────────────────────────────────────────────────────────

    public AnnualEvaluation requireEvaluation(Long evaluationId) {
        return evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new ResourceNotFoundException("AnnualEvaluation", evaluationId));
    }

    private AnnualEvaluationNextCycleGoal requireGoal(Long evaluationId, Long goalId) {
        AnnualEvaluationNextCycleGoal goal = nextCycleGoalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("AnnualEvaluationNextCycleGoal", goalId));
        if (!goal.getEvaluation().getId().equals(evaluationId)) {
            throw new ResourceNotFoundException("AnnualEvaluationNextCycleGoal", goalId);
        }
        return goal;
    }

    private void assertHeadCanEdit(AnnualEvaluation evaluation, Long currentUserId) {
        if (!evaluation.getHead().getId().equals(currentUserId)) {
            throw new UnauthorizedException("Only this employee's head can manage next cycle goals");
        }
        if (evaluation.getState() != AnnualEvaluationState.EMPLOYEE_SUBMITTED
                && evaluation.getState() != AnnualEvaluationState.HEAD_SUBMITTED) {
            throw new BusinessRuleException("The head cannot edit next cycle goals yet -- the employee must submit their self-assessment first");
        }
        if (evaluation.isLocked()) {
            throw new BusinessRuleException("This evaluation is locked -- a signature has already been recorded");
        }
    }

    private void assertEmployeeCanReview(AnnualEvaluation evaluation, Long currentUserId) {
        if (!evaluation.getEmployee().getId().equals(currentUserId)) {
            throw new UnauthorizedException("Only this evaluation's employee can review next cycle goals");
        }
        if (evaluation.getState() != AnnualEvaluationState.HEAD_SUBMITTED) {
            throw new BusinessRuleException("You can only review next cycle goals after the head has submitted their evaluation");
        }
        if (evaluation.isLocked()) {
            throw new BusinessRuleException("This evaluation is locked -- you've already signed or refused");
        }
    }

    private void assertCanView(AnnualEvaluation evaluation, Long currentUserId) {
        boolean allowed = evaluation.getEmployee().getId().equals(currentUserId)
                || evaluation.getHead().getId().equals(currentUserId);
        if (!allowed) {
            AppUser currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));
            allowed = currentUser.hasRole(SystemRole.ADMIN);
        }
        if (!allowed) {
            throw new UnauthorizedException("You do not have access to this evaluation's next cycle goals");
        }
    }
}
