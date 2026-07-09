package com.rit.spms.service;

import com.rit.spms.config.SwotProperties;
import com.rit.spms.domain.AppUser;
import com.rit.spms.domain.RoleAssignment;
import com.rit.spms.domain.Strategy;
import com.rit.spms.domain.SwotEntry;
import com.rit.spms.domain.SwotParticipant;
import com.rit.spms.domain.SwotSession;
import com.rit.spms.domain.enums.RoleType;
import com.rit.spms.domain.enums.SwotPhase;
import com.rit.spms.domain.enums.SwotQuadrant;
import com.rit.spms.dto.response.SwotEntryResponse;
import com.rit.spms.dto.response.SwotJustificationResponse;
import com.rit.spms.dto.response.SwotPendingActionResponse;
import com.rit.spms.dto.response.SwotStatusResponse;
import com.rit.spms.dto.response.SwotVisualizationWordResponse;
import com.rit.spms.exception.BusinessRuleException;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.exception.UnauthorizedException;
import com.rit.spms.repository.AppUserRepository;
import com.rit.spms.repository.RoleAssignmentRepository;
import com.rit.spms.repository.StrategyRepository;
import com.rit.spms.repository.SwotEntryRepository;
import com.rit.spms.repository.SwotParticipantRepository;
import com.rit.spms.repository.SwotSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/** Word-collection phase of the SWOT workflow: starting a session, submitting words, and the quadrant visualization. */
@Service
@RequiredArgsConstructor
@Transactional
public class SwotService {

    private final SwotSessionRepository swotSessionRepository;
    private final SwotParticipantRepository swotParticipantRepository;
    private final SwotEntryRepository swotEntryRepository;
    private final RoleAssignmentRepository roleAssignmentRepository;
    private final StrategyRepository strategyRepository;
    private final AppUserRepository appUserRepository;
    private final PermissionService permissionService;
    private final AuditService auditService;
    private final SwotProperties swotProperties;
    private final ApplicationEventPublisher eventPublisher;

    public SwotSession startSwot(Long strategyId, Long ownerId) {
        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy", strategyId));
        AppUser owner = appUserRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", ownerId));

        SwotSession session = swotSessionRepository.save(SwotSession.builder()
                .strategy(strategy)
                .startedBy(owner)
                .build());

        List<RoleAssignment> assignments = roleAssignmentRepository
                .findByStrategyIdAndRoleIn(strategyId, List.of(RoleType.OWNER, RoleType.EDITOR));
        for (RoleAssignment ra : assignments) {
            swotParticipantRepository.save(SwotParticipant.builder()
                    .swotSession(session)
                    .user(ra.getUser())
                    .roleAtInvite(ra.getRole())
                    .build());
            if (!ra.getUser().getId().equals(ownerId)) {
                eventPublisher.publishEvent(new SwotInviteEvent(strategyId, ra.getUser().getId()));
            }
        }

        auditService.log(owner, "SWOT_STARTED", "SwotSession", session.getId(), strategy,
                "Started SWOT analysis with " + assignments.size() + " participants");
        return session;
    }

    @Transactional(readOnly = true)
    public SwotStatusResponse getStatus(Long strategyId, Long userId) {
        boolean isOwner = permissionService.isOwner(userId, strategyId);
        Optional<SwotSession> sessionOpt = swotSessionRepository.findByStrategyId(strategyId);
        if (sessionOpt.isEmpty()) {
            return SwotStatusResponse.builder()
                    .sessionStarted(false)
                    .owner(isOwner)
                    .participant(false)
                    .build();
        }
        SwotSession session = sessionOpt.get();
        Optional<SwotParticipant> me = swotParticipantRepository
                .findBySwotSessionIdAndUserId(session.getId(), userId);
        return SwotStatusResponse.builder()
                .sessionStarted(true)
                .phase(session.getPhase())
                .generationRequestedAt(session.getGenerationRequestedAt())
                .generationFailureReason(session.getGenerationFailureReason())
                .owner(isOwner)
                .participant(me.isPresent())
                .totalParticipants(swotParticipantRepository.countBySwotSessionId(session.getId()))
                .submittedCount(swotParticipantRepository.countBySwotSessionIdAndSwotSubmittedAtIsNotNull(session.getId()))
                .votedCount(swotParticipantRepository.countBySwotSessionIdAndVoteSubmittedAtIsNotNull(session.getId()))
                .nonOwnerParticipants(swotParticipantRepository
                        .countBySwotSessionIdAndRoleAtInviteNot(session.getId(), RoleType.OWNER))
                .reviewedCount(swotParticipantRepository
                        .countBySwotSessionIdAndRoleAtInviteNotAndReviewSubmittedAtIsNotNull(session.getId(), RoleType.OWNER))
                .mySwotSubmitted(me.map(p -> p.getSwotSubmittedAt() != null).orElse(false))
                .myVoteSubmitted(me.map(p -> p.getVoteSubmittedAt() != null).orElse(false))
                .myReviewSubmitted(me.map(p -> p.getReviewSubmittedAt() != null).orElse(false))
                .build();
    }

    public SwotEntryResponse submitWord(Long strategyId, Long userId, SwotQuadrant quadrant, String word, String justification) {
        SwotSession session = requireSession(strategyId);
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", userId));

        String normalized = normalize(word);
        if (swotEntryRepository.existsBySwotSessionIdAndUserIdAndQuadrantAndNormalizedWord(
                session.getId(), userId, quadrant, normalized)) {
            throw new BusinessRuleException("You've already added \"" + word + "\" to this quadrant");
        }
        long existingCount = swotEntryRepository.countBySwotSessionIdAndUserIdAndQuadrant(session.getId(), userId, quadrant);
        if (existingCount >= swotProperties.getMaxWordsPerQuadrant()) {
            throw new BusinessRuleException("Maximum of " + swotProperties.getMaxWordsPerQuadrant() + " words per quadrant");
        }

        SwotEntry entry = swotEntryRepository.save(SwotEntry.builder()
                .swotSession(session)
                .user(user)
                .quadrant(quadrant)
                .word(word.trim())
                .normalizedWord(normalized)
                .justification(justification.trim())
                .sortOrder((int) existingCount)
                .build());

        auditService.log(user, "SWOT_WORD_SUBMITTED", "SwotEntry", entry.getId(), session.getStrategy(),
                quadrant + ": " + word);
        return toEntryResponse(entry);
    }

    public void deleteWord(Long strategyId, Long userId, Long entryId) {
        SwotSession session = requireSession(strategyId);
        SwotEntry entry = swotEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("SwotEntry", entryId));
        if (!entry.getSwotSession().getId().equals(session.getId()) || !entry.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only remove your own SWOT words");
        }
        swotEntryRepository.delete(entry);
    }

    @Transactional(readOnly = true)
    public List<SwotEntryResponse> getMyEntries(Long strategyId, Long userId, SwotQuadrant quadrant) {
        SwotSession session = requireSession(strategyId);
        return swotEntryRepository.findBySwotSessionIdAndUserIdOrderByQuadrantAscSortOrderAsc(session.getId(), userId)
                .stream()
                .filter(e -> quadrant == null || e.getQuadrant() == quadrant)
                .map(this::toEntryResponse)
                .collect(Collectors.toList());
    }

    public SwotStatusResponse submitFullSwot(Long strategyId, Long userId) {
        SwotSession session = requireSession(strategyId);
        SwotParticipant participant = swotParticipantRepository
                .findBySwotSessionIdAndUserId(session.getId(), userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a participant in this SWOT session"));

        for (SwotQuadrant quadrant : SwotQuadrant.values()) {
            long count = swotEntryRepository.countBySwotSessionIdAndUserIdAndQuadrant(session.getId(), userId, quadrant);
            if (count < swotProperties.getMinWordsPerQuadrant()) {
                throw new BusinessRuleException("At least " + swotProperties.getMinWordsPerQuadrant()
                        + " words are required in " + quadrant + " before submitting");
            }
        }

        participant.setSwotSubmittedAt(LocalDateTime.now());
        swotParticipantRepository.save(participant);
        auditService.log(participant.getUser(), "SWOT_ANALYSIS_SUBMITTED", "SwotSession", session.getId(),
                session.getStrategy(), "Submitted full SWOT analysis");

        long total = swotParticipantRepository.countBySwotSessionId(session.getId());
        long submitted = swotParticipantRepository.countBySwotSessionIdAndSwotSubmittedAtIsNotNull(session.getId());
        if (submitted >= total) {
            session.setPhase(SwotPhase.VOTING);
            swotSessionRepository.save(session);
            auditService.log(participant.getUser(), "SWOT_LOCKED_FOR_VOTING", "SwotSession", session.getId(),
                    session.getStrategy(), "All participants submitted; voting is now open");
        }
        return getStatus(strategyId, userId);
    }

    @Transactional(readOnly = true)
    public List<SwotVisualizationWordResponse> getVisualization(Long strategyId, SwotQuadrant quadrantFilter) {
        SwotSession session = requireSession(strategyId);
        List<SwotEntry> entries = swotEntryRepository.findBySwotSessionIdOrderByQuadrantAscCreatedAtAsc(session.getId());

        Map<SwotQuadrant, Map<String, List<SwotEntry>>> grouped = new EnumMap<>(SwotQuadrant.class);
        for (SwotEntry e : entries) {
            if (quadrantFilter != null && e.getQuadrant() != quadrantFilter) continue;
            grouped.computeIfAbsent(e.getQuadrant(), q -> new LinkedHashMap<>())
                    .computeIfAbsent(e.getNormalizedWord(), w -> new ArrayList<>())
                    .add(e);
        }

        List<SwotVisualizationWordResponse> result = new ArrayList<>();
        for (var quadrantEntry : grouped.entrySet()) {
            for (var wordEntry : quadrantEntry.getValue().entrySet()) {
                List<SwotEntry> group = wordEntry.getValue();
                List<SwotJustificationResponse> justifications = group.stream()
                        .map(e -> SwotJustificationResponse.builder()
                                .contributorName(e.getUser().getFname() + " " + e.getUser().getLname())
                                .sentence(e.getJustification())
                                .build())
                        .collect(Collectors.toList());
                result.add(SwotVisualizationWordResponse.builder()
                        .quadrant(quadrantEntry.getKey())
                        .word(group.get(0).getWord())
                        .submitterCount((int) group.stream().map(e -> e.getUser().getId()).distinct().count())
                        .justifications(justifications)
                        .build());
            }
        }
        return result;
    }

    /** Strategies where this user is a SWOT participant and currently owes an action. */
    @Transactional(readOnly = true)
    public List<SwotPendingActionResponse> getPendingActions(Long userId) {
        List<SwotParticipant> mine = swotParticipantRepository.findByUserIdWithSession(userId);
        List<SwotPendingActionResponse> pending = new ArrayList<>();
        for (SwotParticipant p : mine) {
            SwotSession session = p.getSwotSession();
            String action = switch (session.getPhase()) {
                case COLLECTING -> p.getSwotSubmittedAt() == null ? "SUBMIT_SWOT" : null;
                case VOTING -> p.getVoteSubmittedAt() == null ? "VOTE" : null;
                case REVIEWING -> p.getReviewSubmittedAt() == null ? "REVIEW" : null;
                case FINALIZING -> p.getRoleAtInvite() == RoleType.OWNER ? "FINALIZE" : null;
                default -> null;
            };
            if (action == null) continue;
            pending.add(SwotPendingActionResponse.builder()
                    .strategyId(session.getStrategy().getId())
                    .strategyTitle(session.getStrategy().getTitle())
                    .phase(session.getPhase())
                    .actionNeeded(action)
                    .build());
        }
        return pending;
    }

    private SwotSession requireSession(Long strategyId) {
        return swotSessionRepository.findByStrategyId(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("SwotSession for strategy", strategyId));
    }

    static String normalize(String word) {
        return word.trim().toLowerCase();
    }

    private SwotEntryResponse toEntryResponse(SwotEntry e) {
        return SwotEntryResponse.builder()
                .id(e.getId())
                .quadrant(e.getQuadrant())
                .word(e.getWord())
                .justification(e.getJustification())
                .sortOrder(e.getSortOrder())
                .build();
    }
}
