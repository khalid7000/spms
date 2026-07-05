package com.rit.spms.service;

import com.rit.spms.config.SwotProperties;
import com.rit.spms.domain.SwotEntry;
import com.rit.spms.domain.SwotParticipant;
import com.rit.spms.domain.SwotQuadrantResult;
import com.rit.spms.domain.SwotSession;
import com.rit.spms.domain.SwotVoteEntry;
import com.rit.spms.domain.enums.SwotPhase;
import com.rit.spms.domain.enums.SwotQuadrant;
import com.rit.spms.dto.response.SwotBallotCandidateResponse;
import com.rit.spms.dto.response.SwotResultResponse;
import com.rit.spms.dto.response.SwotVoteBallotResponse;
import com.rit.spms.exception.BusinessRuleException;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.exception.UnauthorizedException;
import com.rit.spms.repository.SwotEntryRepository;
import com.rit.spms.repository.SwotParticipantRepository;
import com.rit.spms.repository.SwotQuadrantResultRepository;
import com.rit.spms.repository.SwotSessionRepository;
import com.rit.spms.repository.SwotVoteEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Ranked-choice voting on SWOT words: ballots, submission, weighted tally, and results.
 *
 * Candidate words are grouped into synonym families via {@link SwotWordClusterer} before
 * being shown as ballot options — see that class for why the same clustering has to be
 * recomputed identically at ballot-build, vote-validation, and tally time.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SwotVotingService {

    private final SwotSessionRepository swotSessionRepository;
    private final SwotParticipantRepository swotParticipantRepository;
    private final SwotEntryRepository swotEntryRepository;
    private final SwotVoteEntryRepository swotVoteEntryRepository;
    private final SwotQuadrantResultRepository swotQuadrantResultRepository;
    private final AuditService auditService;
    private final SwotProperties swotProperties;
    private final SwotWordClusterer wordClusterer;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public SwotVoteBallotResponse getBallot(Long strategyId, SwotQuadrant quadrant) {
        SwotSession session = requireSession(strategyId);
        List<SwotEntry> entries = swotEntryRepository.findBySwotSessionIdAndQuadrant(session.getId(), quadrant);

        // Sorted by submitter count descending — the closest signal to "popularity" available
        // before any votes exist. A combined synonym-family option naturally floats near the top
        // too, since its count is the sum of its members'.
        List<SwotBallotCandidateResponse> candidates = wordClusterer.cluster(entries).stream()
                .sorted(Comparator
                        .comparingInt(SwotWordClusterer.WordCluster::submitterCount).reversed()
                        .thenComparing(SwotWordClusterer.WordCluster::displayLabel))
                .map(c -> SwotBallotCandidateResponse.builder()
                        .word(c.key())
                        .displayLabel(c.displayLabel())
                        .submitterCount(c.submitterCount())
                        .synonymGroup(c.isGroup())
                        .relatedWords(c.relatedWords())
                        .build())
                .collect(Collectors.toList());

        return SwotVoteBallotResponse.builder()
                .quadrant(quadrant)
                .rankCount(swotProperties.getVoteRankCount())
                .candidates(candidates)
                .build();
    }

    public void submitVotes(Long strategyId, Long userId, Map<SwotQuadrant, List<String>> rankedWordsByQuadrant) {
        SwotSession session = requireSession(strategyId);
        SwotParticipant participant = swotParticipantRepository.findBySwotSessionIdAndUserId(session.getId(), userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a participant in this SWOT session"));

        int rankCount = swotProperties.getVoteRankCount();
        List<SwotVoteEntry> toSave = new ArrayList<>();
        for (Map.Entry<SwotQuadrant, List<String>> quadrantVotes : rankedWordsByQuadrant.entrySet()) {
            SwotQuadrant quadrant = quadrantVotes.getKey();
            List<String> ranked = quadrantVotes.getValue();
            if (ranked.isEmpty() || ranked.size() > rankCount) {
                throw new BusinessRuleException("Rank between 1 and " + rankCount + " words for " + quadrant);
            }
            // Valid values include both plain words AND synthetic combined-family keys — both are
            // produced by the same clusterer used to build the ballot, so a value the frontend got
            // from getBallot() always validates here unchanged.
            List<SwotEntry> quadrantEntries = swotEntryRepository.findBySwotSessionIdAndQuadrant(session.getId(), quadrant);
            Set<String> validWords = wordClusterer.cluster(quadrantEntries).stream()
                    .map(SwotWordClusterer.WordCluster::key)
                    .collect(Collectors.toSet());
            Set<String> seen = new HashSet<>();
            for (int i = 0; i < ranked.size(); i++) {
                String normalized = SwotService.normalize(ranked.get(i));
                if (!validWords.contains(normalized)) {
                    throw new BusinessRuleException("\"" + ranked.get(i) + "\" is not a candidate word in " + quadrant);
                }
                if (!seen.add(normalized)) {
                    throw new BusinessRuleException("Duplicate word \"" + ranked.get(i) + "\" in your " + quadrant + " ranking");
                }
                toSave.add(SwotVoteEntry.builder()
                        .swotSession(session)
                        .user(participant.getUser())
                        .quadrant(quadrant)
                        .rank(i + 1)
                        .normalizedWord(normalized)
                        .build());
            }
        }

        swotVoteEntryRepository.deleteBySwotSessionIdAndUserId(session.getId(), userId);
        swotVoteEntryRepository.saveAll(toSave);

        participant.setVoteSubmittedAt(LocalDateTime.now());
        swotParticipantRepository.save(participant);
        auditService.log(participant.getUser(), "SWOT_VOTE_SUBMITTED", "SwotSession", session.getId(),
                session.getStrategy(), "Submitted ranked SWOT vote");

        long total = swotParticipantRepository.countBySwotSessionId(session.getId());
        long voted = swotParticipantRepository.countBySwotSessionIdAndVoteSubmittedAtIsNotNull(session.getId());
        if (voted >= total) {
            tallyResults(session);
        }
    }

    private void tallyResults(SwotSession session) {
        List<Integer> weights = swotProperties.getVoteRankWeights();
        List<SwotVoteEntry> votes = swotVoteEntryRepository.findBySwotSessionId(session.getId());
        List<SwotEntry> entries = swotEntryRepository.findBySwotSessionIdOrderByQuadrantAscCreatedAtAsc(session.getId());

        // Re-cluster per quadrant so a vote cast for a combined-family key (e.g. "grp:innovation+invention")
        // resolves to a proper display label and the summed submitter count for tie-breaking, exactly
        // like an individually-voted word would — both are just candidate keys from the same clusterer.
        Map<SwotQuadrant, Map<String, String>> displayWordByQuadrant = new EnumMap<>(SwotQuadrant.class);
        Map<SwotQuadrant, Map<String, Long>> submitterCountByQuadrant = new EnumMap<>(SwotQuadrant.class);
        for (SwotQuadrant quadrant : SwotQuadrant.values()) {
            List<SwotEntry> quadrantEntries = entries.stream().filter(e -> e.getQuadrant() == quadrant).toList();
            Map<String, String> displayMap = new HashMap<>();
            Map<String, Long> countMap = new HashMap<>();
            for (SwotWordClusterer.WordCluster c : wordClusterer.cluster(quadrantEntries)) {
                displayMap.put(c.key(), c.displayLabel());
                countMap.put(c.key(), (long) c.submitterCount());
            }
            displayWordByQuadrant.put(quadrant, displayMap);
            submitterCountByQuadrant.put(quadrant, countMap);
        }

        Map<SwotQuadrant, Map<String, Integer>> scoreByQuadrant = new EnumMap<>(SwotQuadrant.class);
        for (SwotVoteEntry v : votes) {
            int weight = v.getRank() - 1 < weights.size() ? weights.get(v.getRank() - 1) : 0;
            scoreByQuadrant.computeIfAbsent(v.getQuadrant(), q -> new HashMap<>())
                    .merge(v.getNormalizedWord(), weight, Integer::sum);
        }

        swotQuadrantResultRepository.deleteBySwotSessionId(session.getId());
        for (SwotQuadrant quadrant : SwotQuadrant.values()) {
            Map<String, Integer> scores = scoreByQuadrant.getOrDefault(quadrant, Map.of());
            Map<String, Long> submitterCounts = submitterCountByQuadrant.getOrDefault(quadrant, Map.of());
            List<Map.Entry<String, Integer>> ranked = scores.entrySet().stream()
                    .sorted(Comparator
                            .<Map.Entry<String, Integer>>comparingInt(Map.Entry::getValue).reversed()
                            .thenComparing((a, b) -> Long.compare(
                                    submitterCounts.getOrDefault(b.getKey(), 0L),
                                    submitterCounts.getOrDefault(a.getKey(), 0L)))
                            .thenComparing(Map.Entry::getKey))
                    .collect(Collectors.toList());

            int position = 1;
            for (Map.Entry<String, Integer> entry : ranked) {
                String displayWord = displayWordByQuadrant.getOrDefault(quadrant, Map.of())
                        .getOrDefault(entry.getKey(), entry.getKey());
                swotQuadrantResultRepository.save(SwotQuadrantResult.builder()
                        .swotSession(session)
                        .quadrant(quadrant)
                        .normalizedWord(entry.getKey())
                        .displayWord(displayWord)
                        .totalScore(entry.getValue())
                        .rankPosition(position++)
                        .build());
            }
        }

        session.setPhase(SwotPhase.GENERATING_SUGGESTIONS);
        session.setVotingClosedAt(LocalDateTime.now());
        // Set here (this transaction commits before the AFTER_COMMIT listener even fires) so
        // pollers see generationRequestedAt immediately, without waiting on the async generation
        // call's own transaction to finish.
        session.setGenerationRequestedAt(LocalDateTime.now());
        session.setGenerationFailureReason(null);
        swotSessionRepository.save(session);
        auditService.log(session.getStartedBy(), "SWOT_VOTING_CLOSED", "SwotSession", session.getId(),
                session.getStrategy(), "All participants voted; results tallied");

        // Published, not called directly: SwotSuggestionService's listener only fires once this
        // method's transaction actually commits (see SwotVotingClosedEvent) — calling it straight
        // from here would let the background generation thread start before the tally above is
        // even visible to it.
        eventPublisher.publishEvent(new SwotVotingClosedEvent(session.getStrategy().getId()));
    }

    @Transactional(readOnly = true)
    public List<SwotResultResponse> getResults(Long strategyId) {
        SwotSession session = requireSession(strategyId);
        return swotQuadrantResultRepository.findBySwotSessionIdOrderByQuadrantAscRankPositionAsc(session.getId())
                .stream()
                .map(r -> SwotResultResponse.builder()
                        .quadrant(r.getQuadrant())
                        .word(r.getDisplayWord())
                        .totalScore(r.getTotalScore())
                        .rankPosition(r.getRankPosition())
                        .build())
                .collect(Collectors.toList());
    }

    private SwotSession requireSession(Long strategyId) {
        return swotSessionRepository.findByStrategyId(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("SwotSession for strategy", strategyId));
    }
}
