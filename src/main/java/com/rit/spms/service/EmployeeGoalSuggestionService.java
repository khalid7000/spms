package com.rit.spms.service;

import com.rit.spms.domain.EmployeeGoalCycle;
import com.rit.spms.domain.EmployeeGoalSuggestion;
import com.rit.spms.domain.PortfolioCategory;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.repository.EmployeeGoalCycleRepository;
import com.rit.spms.repository.EmployeeGoalSuggestionRepository;
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
 * Generates AI-suggested improvement goals from a leader's strength/weakness notes, via the
 * configured {@link PortfolioGoalSuggestionGenerator} -- the same Claude/Ollama plumbing used by
 * the SWOT module's {@link SwotSuggestionService}, and now the same background/poll/retry shape:
 * a synchronous "requested" checkpoint followed by an @Async generation call that never blocks
 * the triggering request, since a local model call can take a minute or more.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmployeeGoalSuggestionService {

    private final EmployeeGoalCycleRepository cycleRepository;
    private final EmployeeGoalSuggestionRepository suggestionRepository;
    private final PortfolioGoalSuggestionGenerator goalGenerator;

    /**
     * Synchronous checkpoint called by the controller's request thread immediately before kicking
     * off the @Async generation below. Commits before that call is even made, so a client polling
     * GET /{id} right afterward already sees generationRequestedAt -- letting the UI show
     * "submitted at X, Y elapsed" for the whole time the background call is running. Also clears
     * any stale failure reason from a prior attempt.
     */
    public void recordGenerationRequested(Long cycleId) {
        EmployeeGoalCycle cycle = cycleRepository.findById(cycleId)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeGoalCycle", cycleId));
        cycle.setGenerationRequestedAt(LocalDateTime.now());
        cycle.setGenerationFailureReason(null);
        cycleRepository.save(cycle);
    }

    /**
     * Fire-and-forget, mirroring SwotSuggestionService.generateSuggestions exactly: @Async hands
     * this to a background thread so the controller returns immediately; the cycle stays in DRAFT
     * until this completes, and GoalSettingPage polls the cycle and picks up suggestionsGeneratedAt
     * whenever it lands. Failures are caught and recorded (not rethrown) so the leader can retry
     * from the same state instead of the request just failing with no persisted explanation.
     *
     * IMPORTANT: @Async only applies on a call that goes through this bean's Spring proxy. The
     * existing call path (EmployeeGoalCycleService, a different bean, calling this injected
     * service) already satisfies that -- no self-invocation risk here, unlike a same-class call.
     *
     * REQUIRES_NEW: this always needs its own transaction, since the controller's own transaction
     * (if any) has already committed/returned by the time this actually runs on another thread.
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void generateSuggestionsAsync(Long cycleId, List<PortfolioCategory> availableCategories) {
        EmployeeGoalCycle cycle = cycleRepository.findById(cycleId)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeGoalCycle", cycleId));

        Map<String, PortfolioCategory> byName = availableCategories.stream()
                .collect(Collectors.toMap(c -> c.getCategoryName().toLowerCase(), c -> c, (a, b) -> a));

        List<PortfolioGoalSuggestionGenerator.SuggestedGoalDto> suggestions;
        try {
            suggestions = goalGenerator.generateGoalSuggestions(
                    cycle.getLeaderStrengths(), cycle.getLeaderWeaknesses(), availableCategories);
        } catch (Exception e) {
            log.warn("AI goal-suggestion generation failed for cycle {}; cycle stays in DRAFT for retry", cycleId, e);
            cycle.setGenerationFailureReason(failureReason(e));
            cycleRepository.save(cycle);
            return;
        }

        suggestionRepository.deleteByCycleId(cycleId);

        int sortOrder = 0;
        for (PortfolioGoalSuggestionGenerator.SuggestedGoalDto dto : suggestions) {
            PortfolioCategory category = byName.get(dto.categoryName() == null ? "" : dto.categoryName().toLowerCase());
            if (category == null) {
                // AI proposed a category outside the employee's set -- skip rather than fail the whole batch.
                log.warn("Skipping suggestion '{}': category '{}' is not one of this employee's categories",
                        dto.title(), dto.categoryName());
                continue;
            }
            suggestionRepository.save(EmployeeGoalSuggestion.builder()
                    .cycle(cycle)
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

        cycle.setSuggestionsGeneratedAt(LocalDateTime.now());
        cycle.setGenerationFailureReason(null);
        cycleRepository.save(cycle);
    }

    /** A short, user-presentable summary of why the AI call failed -- falls back to the exception's class name. */
    private static String failureReason(Exception e) {
        String msg = e.getMessage();
        return (msg == null || msg.isBlank()) ? e.getClass().getSimpleName() : msg;
    }
}
