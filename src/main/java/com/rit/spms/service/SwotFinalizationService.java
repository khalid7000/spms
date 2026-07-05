package com.rit.spms.service;

import com.rit.spms.domain.AppUser;
import com.rit.spms.domain.Goal;
import com.rit.spms.domain.SwotAlternativeProposal;
import com.rit.spms.domain.SwotAlternativeProposedGoal;
import com.rit.spms.domain.SwotReviewItem;
import com.rit.spms.domain.SwotSession;
import com.rit.spms.domain.SwotSuggestedGoal;
import com.rit.spms.domain.SwotSuggestedGoalAddition;
import com.rit.spms.domain.SwotSuggestion;
import com.rit.spms.domain.VisionArea;
import com.rit.spms.domain.enums.SwotPhase;
import com.rit.spms.domain.enums.SwotReviewActionType;
import com.rit.spms.domain.enums.SwotReviewTargetType;
import com.rit.spms.dto.request.SwotFinalDecisionRequest;
import com.rit.spms.dto.response.SwotFinalizationResultResponse;
import com.rit.spms.exception.BusinessRuleException;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.exception.UnauthorizedException;
import com.rit.spms.repository.AppUserRepository;
import com.rit.spms.repository.GoalRepository;
import com.rit.spms.repository.SwotAlternativeProposalRepository;
import com.rit.spms.repository.SwotAlternativeProposedGoalRepository;
import com.rit.spms.repository.SwotReviewItemRepository;
import com.rit.spms.repository.SwotSessionRepository;
import com.rit.spms.repository.SwotSuggestedGoalAdditionRepository;
import com.rit.spms.repository.SwotSuggestedGoalRepository;
import com.rit.spms.repository.SwotSuggestionRepository;
import com.rit.spms.repository.VisionAreaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Owner-only finalization — step 9/10 of the workflow. Reads the owner's final
 * {@code SwotReviewItem} decisions and creates the real {@link VisionArea}/{@link Goal}
 * rows the existing strategy editor already knows how to render.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SwotFinalizationService {

    private final SwotSessionRepository swotSessionRepository;
    private final SwotReviewItemRepository swotReviewItemRepository;
    private final SwotSuggestionRepository swotSuggestionRepository;
    private final SwotSuggestedGoalRepository swotSuggestedGoalRepository;
    private final SwotSuggestedGoalAdditionRepository swotSuggestedGoalAdditionRepository;
    private final SwotAlternativeProposalRepository swotAlternativeProposalRepository;
    private final SwotAlternativeProposedGoalRepository swotAlternativeProposedGoalRepository;
    private final VisionAreaRepository visionAreaRepository;
    private final GoalRepository goalRepository;
    private final AppUserRepository appUserRepository;
    private final AuditService auditService;

    public void saveDraftDecisions(Long strategyId, Long ownerId, List<SwotFinalDecisionRequest> decisions) {
        SwotSession session = requireSession(strategyId);
        AppUser owner = appUserRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", ownerId));

        for (SwotFinalDecisionRequest d : decisions) {
            validateTarget(session, d.getTargetType(), d.getTargetId());
            SwotReviewItem item = swotReviewItemRepository
                    .findBySwotSessionIdAndReviewerIdAndTargetTypeAndTargetIdAndIsOwnerFinal(
                            session.getId(), ownerId, d.getTargetType(), d.getTargetId(), true)
                    .orElse(SwotReviewItem.builder()
                            .swotSession(session)
                            .reviewer(owner)
                            .targetType(d.getTargetType())
                            .targetId(d.getTargetId())
                            .isOwnerFinal(true)
                            .build());
            item.setActionType(d.getActionType());
            item.setEditedTitle(d.getEditedTitle());
            item.setEditedDescription(d.getEditedDescription());
            swotReviewItemRepository.save(item);
        }
        auditService.log(owner, "SWOT_FINAL_DECISIONS_SAVED", "SwotSession", session.getId(), session.getStrategy(),
                "Saved " + decisions.size() + " draft final decisions");
    }

    public SwotFinalizationResultResponse finalize(Long strategyId, Long ownerId) {
        SwotSession session = requireSession(strategyId);
        AppUser owner = appUserRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", ownerId));

        List<SwotSuggestion> suggestions = swotSuggestionRepository.findBySwotSessionIdOrderBySortOrder(session.getId());
        List<SwotAlternativeProposal> alternatives = swotAlternativeProposalRepository
                .findBySwotSessionIdOrderByCreatedAt(session.getId());

        Map<String, SwotReviewItem> decisionByTarget = swotReviewItemRepository
                .findBySwotSessionIdAndIsOwnerFinal(session.getId(), true).stream()
                .collect(Collectors.toMap(i -> i.getTargetType() + ":" + i.getTargetId(), i -> i));

        List<Long> createdAreaIds = new ArrayList<>();
        List<Long> createdGoalIds = new ArrayList<>();
        int areaSort = 0;

        for (SwotSuggestion s : suggestions) {
            SwotReviewItem decision = decisionByTarget.get(SwotReviewTargetType.AREA + ":" + s.getId());
            if (decision == null) {
                throw new BusinessRuleException("A final decision is required for area \"" + s.getName() + "\"");
            }
            if (decision.getActionType() == SwotReviewActionType.REJECT) {
                continue;
            }
            VisionArea area = visionAreaRepository.save(VisionArea.builder()
                    .strategy(session.getStrategy())
                    .name(resolvedTitle(decision, s.getName()))
                    .sortOrder(areaSort++)
                    .createdBy(owner)
                    .build());
            createdAreaIds.add(area.getId());

            int goalSort = 0;
            for (SwotSuggestedGoal g : swotSuggestedGoalRepository.findBySwotSuggestionIdOrderBySortOrder(s.getId())) {
                SwotReviewItem goalDecision = decisionByTarget.get(SwotReviewTargetType.GOAL + ":" + g.getId());
                if (goalDecision == null) {
                    throw new BusinessRuleException("A final decision is required for goal \"" + g.getTitle() + "\"");
                }
                if (goalDecision.getActionType() == SwotReviewActionType.REJECT) {
                    continue;
                }
                Goal goal = goalRepository.save(Goal.builder()
                        .strategy(session.getStrategy())
                        .area(area)
                        .title(resolvedTitle(goalDecision, g.getTitle()))
                        .description(resolvedDescription(goalDecision, g.getDescription()))
                        .sortOrder(goalSort++)
                        .createdBy(owner)
                        .build());
                createdGoalIds.add(goal.getId());
            }

            // Proposed additions are opt-in like alternative proposals: no decision (or REJECT) just
            // excludes them, rather than blocking finalization the way a missing AI-goal decision does.
            for (SwotSuggestedGoalAddition addition : swotSuggestedGoalAdditionRepository
                    .findBySwotSuggestionIdOrderBySortOrder(s.getId())) {
                SwotReviewItem additionDecision = decisionByTarget.get(SwotReviewTargetType.GOAL_ADDITION + ":" + addition.getId());
                if (additionDecision == null || additionDecision.getActionType() == SwotReviewActionType.REJECT) {
                    continue;
                }
                Goal goal = goalRepository.save(Goal.builder()
                        .strategy(session.getStrategy())
                        .area(area)
                        .title(resolvedTitle(additionDecision, addition.getTitle()))
                        .description(resolvedDescription(additionDecision, addition.getDescription()))
                        .sortOrder(goalSort++)
                        .createdBy(owner)
                        .build());
                createdGoalIds.add(goal.getId());
            }
        }

        // Alternative proposals are opt-in: only those the owner explicitly approves are created.
        for (SwotAlternativeProposal p : alternatives) {
            SwotReviewItem decision = decisionByTarget.get(SwotReviewTargetType.ALTERNATIVE_AREA + ":" + p.getId());
            if (decision == null || decision.getActionType() == SwotReviewActionType.REJECT) {
                continue;
            }
            VisionArea area = visionAreaRepository.save(VisionArea.builder()
                    .strategy(session.getStrategy())
                    .name(resolvedTitle(decision, p.getName()))
                    .sortOrder(areaSort++)
                    .createdBy(owner)
                    .build());
            createdAreaIds.add(area.getId());

            int goalSort = 0;
            for (SwotAlternativeProposedGoal g : swotAlternativeProposedGoalRepository
                    .findByAlternativeProposalIdOrderBySortOrder(p.getId())) {
                SwotReviewItem goalDecision = decisionByTarget.get(SwotReviewTargetType.ALTERNATIVE_GOAL + ":" + g.getId());
                if (goalDecision == null || goalDecision.getActionType() == SwotReviewActionType.REJECT) {
                    continue;
                }
                Goal goal = goalRepository.save(Goal.builder()
                        .strategy(session.getStrategy())
                        .area(area)
                        .title(resolvedTitle(goalDecision, g.getTitle()))
                        .description(resolvedDescription(goalDecision, g.getDescription()))
                        .sortOrder(goalSort++)
                        .createdBy(owner)
                        .build());
                createdGoalIds.add(goal.getId());
            }
        }

        if (createdAreaIds.isEmpty()) {
            throw new BusinessRuleException("At least one area must be approved to finalize the draft strategy");
        }

        session.setPhase(SwotPhase.COMPLETED);
        session.setFinalizedAt(LocalDateTime.now());
        swotSessionRepository.save(session);
        auditService.log(owner, "SWOT_FINALIZED", "SwotSession", session.getId(), session.getStrategy(),
                "Created " + createdAreaIds.size() + " areas and " + createdGoalIds.size() + " goals from the SWOT workflow");

        return SwotFinalizationResultResponse.builder()
                .createdAreaIds(createdAreaIds)
                .createdGoalIds(createdGoalIds)
                .build();
    }

    private String resolvedTitle(SwotReviewItem decision, String original) {
        return decision.getActionType() == SwotReviewActionType.APPROVE_WITH_EDITS
                && decision.getEditedTitle() != null && !decision.getEditedTitle().isBlank()
                ? decision.getEditedTitle() : original;
    }

    private String resolvedDescription(SwotReviewItem decision, String original) {
        return decision.getActionType() == SwotReviewActionType.APPROVE_WITH_EDITS
                && decision.getEditedDescription() != null
                ? decision.getEditedDescription() : original;
    }

    private void validateTarget(SwotSession session, SwotReviewTargetType targetType, Long targetId) {
        switch (targetType) {
            case AREA -> {
                SwotSuggestion s = swotSuggestionRepository.findById(targetId)
                        .orElseThrow(() -> new ResourceNotFoundException("SwotSuggestion", targetId));
                if (!s.getSwotSession().getId().equals(session.getId())) {
                    throw new UnauthorizedException("Suggestion does not belong to this strategy");
                }
            }
            case GOAL -> {
                SwotSuggestedGoal g = swotSuggestedGoalRepository.findById(targetId)
                        .orElseThrow(() -> new ResourceNotFoundException("SwotSuggestedGoal", targetId));
                if (!g.getSwotSuggestion().getSwotSession().getId().equals(session.getId())) {
                    throw new UnauthorizedException("Suggested goal does not belong to this strategy");
                }
            }
            case GOAL_ADDITION -> {
                SwotSuggestedGoalAddition g = swotSuggestedGoalAdditionRepository.findById(targetId)
                        .orElseThrow(() -> new ResourceNotFoundException("SwotSuggestedGoalAddition", targetId));
                if (!g.getSwotSuggestion().getSwotSession().getId().equals(session.getId())) {
                    throw new UnauthorizedException("Proposed goal does not belong to this strategy");
                }
            }
            case ALTERNATIVE_AREA -> {
                SwotAlternativeProposal p = swotAlternativeProposalRepository.findById(targetId)
                        .orElseThrow(() -> new ResourceNotFoundException("SwotAlternativeProposal", targetId));
                if (!p.getSwotSession().getId().equals(session.getId())) {
                    throw new UnauthorizedException("Alternative proposal does not belong to this strategy");
                }
            }
            case ALTERNATIVE_GOAL -> {
                SwotAlternativeProposedGoal g = swotAlternativeProposedGoalRepository.findById(targetId)
                        .orElseThrow(() -> new ResourceNotFoundException("SwotAlternativeProposedGoal", targetId));
                if (!g.getAlternativeProposal().getSwotSession().getId().equals(session.getId())) {
                    throw new UnauthorizedException("Alternative goal does not belong to this strategy");
                }
            }
        }
    }

    private SwotSession requireSession(Long strategyId) {
        return swotSessionRepository.findByStrategyId(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("SwotSession for strategy", strategyId));
    }
}
