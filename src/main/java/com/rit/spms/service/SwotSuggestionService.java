package com.rit.spms.service;

import com.rit.spms.config.SwotProperties;
import com.rit.spms.domain.Strategy;
import com.rit.spms.domain.SwotQuadrantResult;
import com.rit.spms.domain.SwotSession;
import com.rit.spms.domain.SwotSuggestedGoal;
import com.rit.spms.domain.SwotSuggestion;
import com.rit.spms.domain.enums.SwotPhase;
import com.rit.spms.domain.enums.SwotQuadrant;
import com.rit.spms.dto.response.SwotSuggestedGoalResponse;
import com.rit.spms.dto.response.SwotSuggestionResponse;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.repository.SwotQuadrantResultRepository;
import com.rit.spms.repository.SwotSessionRepository;
import com.rit.spms.repository.SwotSuggestedGoalRepository;
import com.rit.spms.repository.SwotSuggestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Generates AI-suggested focus areas/goals from tallied SWOT results via the configured {@link SwotAreaGoalGenerator}. */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SwotSuggestionService {

    private final SwotSessionRepository swotSessionRepository;
    private final SwotQuadrantResultRepository swotQuadrantResultRepository;
    private final SwotSuggestionRepository swotSuggestionRepository;
    private final SwotSuggestedGoalRepository swotSuggestedGoalRepository;
    private final SwotAreaGoalGenerator areaGoalGenerator;
    private final AuditService auditService;
    private final SwotProperties swotProperties;

    /**
     * Synchronous checkpoint called by an external caller (the controller, on the vote-closing
     * or manual-retry request thread) immediately before kicking off the @Async generation below.
     * This method's own transaction commits before that call is even made, so a client polling
     * GET /status right afterward already sees generationRequestedAt — letting the UI show
     * "submitted at X, Y elapsed" for the whole time the background call is running, not just
     * after it eventually finishes. Also clears any stale failure reason from a prior attempt.
     */
    public void recordGenerationRequested(Long strategyId) {
        SwotSession session = swotSessionRepository.findByStrategyId(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("SwotSession for strategy", strategyId));
        session.setGenerationRequestedAt(LocalDateTime.now());
        session.setGenerationFailureReason(null);
        swotSessionRepository.save(session);
    }

    /**
     * Fire-and-forget: a local model call can take a minute or more, so this must never block the
     * HTTP request that triggered it (the last voter's submission — via {@link SwotSuggestionTrigger}'s
     * event listener — or the owner's manual retry endpoint) — @Async hands it to a background thread
     * and both callers return immediately. The session just stays at GENERATING_SUGGESTIONS until this
     * completes; SwotLandingPage polls status and picks up the REVIEWING transition whenever it lands.
     * Failures are caught and logged here (not rethrown) since there's no caller left waiting to
     * receive them — the owner can retry from the same GENERATING_SUGGESTIONS state.
     *
     * IMPORTANT: @Async only takes effect on a call that goes through this bean's Spring proxy —
     * i.e. called as a dependency injected into some *other* bean. Calling it as a plain `this.`
     * method from within this same class (self-invocation) silently skips the proxy and runs
     * synchronously, which is exactly the bug that motivated this comment: see SwotSuggestionTrigger,
     * which exists purely so the AFTER_COMMIT listener calls this through a real injected reference.
     *
     * REQUIRES_NEW rather than the default propagation: this always needs its own transaction,
     * whether called from that listener (no ambient transaction on this background thread to join)
     * or directly from the retry endpoint.
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void generateSuggestions(Long strategyId) {
        SwotSession session = swotSessionRepository.findByStrategyId(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("SwotSession for strategy", strategyId));
        Strategy strategy = session.getStrategy();

        int topN = swotProperties.getTopWordsPerQuadrantForAi();
        Map<SwotQuadrant, List<SwotQuadrantResult>> topWords = new EnumMap<>(SwotQuadrant.class);
        for (SwotQuadrant quadrant : SwotQuadrant.values()) {
            List<SwotQuadrantResult> results = swotQuadrantResultRepository
                    .findBySwotSessionIdAndQuadrantOrderByRankPositionAsc(session.getId(), quadrant);
            topWords.put(quadrant, results.size() > topN ? results.subList(0, topN) : results);
        }

        List<SwotAreaGoalGenerator.SuggestedAreaDto> suggestions;
        try {
            suggestions = areaGoalGenerator.generateAreasAndGoals(strategy, topWords);
        } catch (Exception e) {
            log.warn("AI suggestion generation failed for strategy {}; session stays at GENERATING_SUGGESTIONS for retry",
                    strategyId, e);
            // Recorded so the owner sees *why* it's stuck instead of only "still waiting" forever —
            // the frontend renders this as an error instead of the normal in-progress message.
            session.setGenerationFailureReason(failureReason(e));
            swotSessionRepository.save(session);
            auditService.log(session.getStartedBy(), "SWOT_SUGGESTIONS_GENERATION_FAILED", "SwotSession", session.getId(),
                    strategy, "AI suggestion generation failed: " + failureReason(e));
            return;
        }

        swotSuggestionRepository.deleteBySwotSessionId(session.getId());
        int areaSort = 0;
        for (SwotAreaGoalGenerator.SuggestedAreaDto area : suggestions) {
            SwotSuggestion suggestion = swotSuggestionRepository.save(SwotSuggestion.builder()
                    .swotSession(session)
                    .name(area.name())
                    .rationale(area.rationale())
                    .sortOrder(areaSort++)
                    .generatedByModel(areaGoalGenerator.providerName())
                    .build());
            int goalSort = 0;
            for (SwotAreaGoalGenerator.SuggestedGoalDto goal : area.goals()) {
                swotSuggestedGoalRepository.save(SwotSuggestedGoal.builder()
                        .swotSuggestion(suggestion)
                        .title(goal.title())
                        .description(goal.description())
                        .sortOrder(goalSort++)
                        .build());
            }
        }

        session.setPhase(SwotPhase.REVIEWING);
        session.setSuggestionsGeneratedAt(LocalDateTime.now());
        session.setGenerationFailureReason(null);
        swotSessionRepository.save(session);
        auditService.log(session.getStartedBy(), "SWOT_SUGGESTIONS_GENERATED", "SwotSession", session.getId(),
                strategy, "Generated " + suggestions.size() + " AI-suggested focus areas");
    }

    /** A short, user-presentable summary of why the AI call failed — falls back to the exception's class name. */
    private static String failureReason(Exception e) {
        String msg = e.getMessage();
        return (msg == null || msg.isBlank()) ? e.getClass().getSimpleName() : msg;
    }

    @Transactional(readOnly = true)
    public List<SwotSuggestionResponse> getSuggestions(Long strategyId) {
        SwotSession session = swotSessionRepository.findByStrategyId(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("SwotSession for strategy", strategyId));
        List<SwotSuggestion> suggestions = swotSuggestionRepository.findBySwotSessionIdOrderBySortOrder(session.getId());
        if (suggestions.isEmpty()) {
            return List.of();
        }
        List<Long> suggestionIds = suggestions.stream().map(SwotSuggestion::getId).collect(Collectors.toList());
        Map<Long, List<SwotSuggestedGoal>> goalsBySuggestion = swotSuggestedGoalRepository
                .findBySwotSuggestionIdInOrderBySortOrder(suggestionIds)
                .stream()
                .collect(Collectors.groupingBy(g -> g.getSwotSuggestion().getId()));

        return suggestions.stream()
                .map(s -> SwotSuggestionResponse.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .rationale(s.getRationale())
                        .sortOrder(s.getSortOrder())
                        .generatedByModel(s.getGeneratedByModel())
                        .goals(goalsBySuggestion.getOrDefault(s.getId(), List.of()).stream()
                                .map(g -> SwotSuggestedGoalResponse.builder()
                                        .id(g.getId())
                                        .title(g.getTitle())
                                        .description(g.getDescription())
                                        .sortOrder(g.getSortOrder())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }
}
