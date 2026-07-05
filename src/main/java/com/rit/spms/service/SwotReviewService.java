package com.rit.spms.service;

import com.rit.spms.domain.AppUser;
import com.rit.spms.domain.SwotAlternativeProposal;
import com.rit.spms.domain.SwotAlternativeProposedGoal;
import com.rit.spms.domain.SwotParticipant;
import com.rit.spms.domain.SwotReviewItem;
import com.rit.spms.domain.SwotSession;
import com.rit.spms.domain.SwotSuggestedGoal;
import com.rit.spms.domain.SwotSuggestedGoalAddition;
import com.rit.spms.domain.SwotSuggestion;
import com.rit.spms.domain.enums.RoleType;
import com.rit.spms.domain.enums.SwotPhase;
import com.rit.spms.domain.enums.SwotReviewActionType;
import com.rit.spms.domain.enums.SwotReviewTargetType;
import com.rit.spms.dto.response.SwotAlternativeGoalResponse;
import com.rit.spms.dto.response.SwotAlternativeProposalResponse;
import com.rit.spms.dto.response.SwotReviewItemResponse;
import com.rit.spms.dto.response.SwotReviewSummaryResponse;
import com.rit.spms.dto.response.SwotSuggestedGoalAdditionResponse;
import com.rit.spms.exception.BusinessRuleException;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.exception.UnauthorizedException;
import com.rit.spms.repository.AppUserRepository;
import com.rit.spms.repository.SwotAlternativeProposalRepository;
import com.rit.spms.repository.SwotAlternativeProposedGoalRepository;
import com.rit.spms.repository.SwotParticipantRepository;
import com.rit.spms.repository.SwotReviewItemRepository;
import com.rit.spms.repository.SwotSessionRepository;
import com.rit.spms.repository.SwotSuggestedGoalAdditionRepository;
import com.rit.spms.repository.SwotSuggestedGoalRepository;
import com.rit.spms.repository.SwotSuggestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Per-user review of AI-suggested (and alternative) focus areas/goals — step 8 of the workflow. */
@Service
@RequiredArgsConstructor
@Transactional
public class SwotReviewService {

    private final SwotSessionRepository swotSessionRepository;
    private final SwotParticipantRepository swotParticipantRepository;
    private final SwotSuggestionRepository swotSuggestionRepository;
    private final SwotSuggestedGoalRepository swotSuggestedGoalRepository;
    private final SwotAlternativeProposalRepository swotAlternativeProposalRepository;
    private final SwotAlternativeProposedGoalRepository swotAlternativeProposedGoalRepository;
    private final SwotSuggestedGoalAdditionRepository swotSuggestedGoalAdditionRepository;
    private final SwotReviewItemRepository swotReviewItemRepository;
    private final AppUserRepository appUserRepository;
    private final AuditService auditService;
    private final SwotSuggestionService swotSuggestionService;

    public SwotReviewItemResponse submitReviewItem(Long strategyId, Long userId, SwotReviewTargetType targetType,
            Long targetId, SwotReviewActionType actionType, String editedTitle, String editedDescription) {
        SwotSession session = requireSession(strategyId);
        AppUser reviewer = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", userId));
        validateTarget(session, targetType, targetId);

        SwotReviewItem item = swotReviewItemRepository
                .findBySwotSessionIdAndReviewerIdAndTargetTypeAndTargetIdAndIsOwnerFinal(
                        session.getId(), userId, targetType, targetId, false)
                .orElse(SwotReviewItem.builder()
                        .swotSession(session)
                        .reviewer(reviewer)
                        .targetType(targetType)
                        .targetId(targetId)
                        .isOwnerFinal(false)
                        .build());
        item.setActionType(actionType);
        item.setEditedTitle(editedTitle);
        item.setEditedDescription(editedDescription);
        item = swotReviewItemRepository.save(item);

        auditService.log(reviewer, "SWOT_REVIEW_ITEM_SUBMITTED", "SwotReviewItem", item.getId(), session.getStrategy(),
                targetType + " " + targetId + " -> " + actionType);
        return toItemResponse(item);
    }

    public SwotAlternativeProposalResponse submitAlternativeProposal(Long strategyId, Long userId, String name,
            String rationale, List<com.rit.spms.dto.request.SwotAlternativeGoalRequest> goals) {
        SwotSession session = requireSession(strategyId);
        AppUser proposer = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", userId));

        SwotAlternativeProposal proposal = swotAlternativeProposalRepository.save(SwotAlternativeProposal.builder()
                .swotSession(session)
                .proposedBy(proposer)
                .name(name)
                .rationale(rationale)
                .build());

        int sort = 0;
        for (var g : goals) {
            swotAlternativeProposedGoalRepository.save(SwotAlternativeProposedGoal.builder()
                    .alternativeProposal(proposal)
                    .title(g.getTitle())
                    .description(g.getDescription())
                    .sortOrder(sort++)
                    .build());
        }

        auditService.log(proposer, "SWOT_ALTERNATIVE_PROPOSED", "SwotAlternativeProposal", proposal.getId(),
                session.getStrategy(), "Proposed alternative area: " + name);
        return toAlternativeResponse(proposal);
    }

    public void deleteAlternativeProposal(Long strategyId, Long userId, Long proposalId) {
        SwotSession session = requireSession(strategyId);
        SwotAlternativeProposal proposal = swotAlternativeProposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("SwotAlternativeProposal", proposalId));
        if (!proposal.getSwotSession().getId().equals(session.getId())
                || !proposal.getProposedBy().getId().equals(userId)) {
            throw new UnauthorizedException("You can only remove your own alternative proposals");
        }
        swotAlternativeProposalRepository.delete(proposal);
    }

    /** Proposes a brand-new goal under an existing AI-suggested area — supplements, not replaces, the AI's own goals. */
    public SwotSuggestedGoalAdditionResponse proposeGoalAddition(Long strategyId, Long userId, Long areaId,
            String title, String description) {
        SwotSession session = requireSession(strategyId);
        SwotSuggestion area = swotSuggestionRepository.findById(areaId)
                .orElseThrow(() -> new ResourceNotFoundException("SwotSuggestion", areaId));
        if (!area.getSwotSession().getId().equals(session.getId())) {
            throw new UnauthorizedException("Suggestion does not belong to this strategy");
        }
        AppUser proposer = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", userId));

        SwotSuggestedGoalAddition addition = swotSuggestedGoalAdditionRepository.save(SwotSuggestedGoalAddition.builder()
                .swotSuggestion(area)
                .proposedBy(proposer)
                .title(title)
                .description(description)
                .build());

        auditService.log(proposer, "SWOT_GOAL_ADDITION_PROPOSED", "SwotSuggestedGoalAddition", addition.getId(),
                session.getStrategy(), "Proposed new goal \"" + title + "\" under area: " + area.getName());
        return toGoalAdditionResponse(addition);
    }

    /** Non-owner callers see only their own proposed additions; the owner sees every Editor's (and their own). */
    @Transactional(readOnly = true)
    public List<SwotSuggestedGoalAdditionResponse> getGoalAdditions(Long strategyId, Long userId, boolean allForOwner) {
        SwotSession session = requireSession(strategyId);
        List<SwotSuggestedGoalAddition> additions = allForOwner
                ? swotSuggestedGoalAdditionRepository.findBySwotSuggestion_SwotSession_IdOrderByCreatedAt(session.getId())
                : swotSuggestedGoalAdditionRepository
                        .findBySwotSuggestion_SwotSession_IdAndProposedByIdOrderByCreatedAt(session.getId(), userId);
        return additions.stream().map(this::toGoalAdditionResponse).collect(Collectors.toList());
    }

    public void deleteGoalAddition(Long strategyId, Long userId, Long additionId) {
        SwotSession session = requireSession(strategyId);
        SwotSuggestedGoalAddition addition = swotSuggestedGoalAdditionRepository.findById(additionId)
                .orElseThrow(() -> new ResourceNotFoundException("SwotSuggestedGoalAddition", additionId));
        if (!addition.getSwotSuggestion().getSwotSession().getId().equals(session.getId())
                || !addition.getProposedBy().getId().equals(userId)) {
            throw new UnauthorizedException("You can only remove your own proposed goals");
        }
        swotSuggestedGoalAdditionRepository.delete(addition);
    }

    @Transactional(readOnly = true)
    public List<SwotReviewItemResponse> getMyReviewItems(Long strategyId, Long userId) {
        SwotSession session = requireSession(strategyId);
        return swotReviewItemRepository
                .findBySwotSessionIdAndReviewerIdAndIsOwnerFinal(session.getId(), userId, false)
                .stream().map(this::toItemResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SwotAlternativeProposalResponse> getMyAlternatives(Long strategyId, Long userId, boolean allForOwner) {
        SwotSession session = requireSession(strategyId);
        List<SwotAlternativeProposal> proposals = allForOwner
                ? swotAlternativeProposalRepository.findBySwotSessionIdOrderByCreatedAt(session.getId())
                : swotAlternativeProposalRepository.findBySwotSessionIdAndProposedById(session.getId(), userId);
        return proposals.stream().map(this::toAlternativeResponse).collect(Collectors.toList());
    }

    public void submitFullReview(Long strategyId, Long userId) {
        SwotSession session = requireSession(strategyId);
        SwotParticipant participant = swotParticipantRepository.findBySwotSessionIdAndUserId(session.getId(), userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a participant in this SWOT session"));

        Set<String> reviewed = swotReviewItemRepository
                .findBySwotSessionIdAndReviewerIdAndIsOwnerFinal(session.getId(), userId, false)
                .stream()
                .map(i -> i.getTargetType() + ":" + i.getTargetId())
                .collect(Collectors.toSet());

        List<SwotSuggestion> suggestions = swotSuggestionRepository.findBySwotSessionIdOrderBySortOrder(session.getId());
        for (SwotSuggestion s : suggestions) {
            String areaKey = SwotReviewTargetType.AREA + ":" + s.getId();
            if (!reviewed.contains(areaKey)) {
                throw new BusinessRuleException("You must review every suggested area before submitting (missing: " + s.getName() + ")");
            }
            SwotReviewItem areaItem = swotReviewItemRepository
                    .findBySwotSessionIdAndReviewerIdAndTargetTypeAndTargetIdAndIsOwnerFinal(
                            session.getId(), userId, SwotReviewTargetType.AREA, s.getId(), false).orElseThrow();
            if (areaItem.getActionType() == SwotReviewActionType.REJECT) {
                continue;
            }
            for (SwotSuggestedGoal g : swotSuggestedGoalRepository.findBySwotSuggestionIdOrderBySortOrder(s.getId())) {
                String goalKey = SwotReviewTargetType.GOAL + ":" + g.getId();
                if (!reviewed.contains(goalKey)) {
                    throw new BusinessRuleException("You must review every goal under \"" + s.getName()
                            + "\" before submitting (missing: " + g.getTitle() + ")");
                }
            }
        }

        participant.setReviewSubmittedAt(LocalDateTime.now());
        swotParticipantRepository.save(participant);
        auditService.log(participant.getUser(), "SWOT_REVIEW_SUBMITTED", "SwotSession", session.getId(),
                session.getStrategy(), "Submitted suggestion review");

        long nonOwnerTotal = swotParticipantRepository
                .countBySwotSessionIdAndRoleAtInviteNot(session.getId(), RoleType.OWNER);
        long nonOwnerReviewed = swotParticipantRepository
                .countBySwotSessionIdAndRoleAtInviteNotAndReviewSubmittedAtIsNotNull(session.getId(), RoleType.OWNER);
        if (nonOwnerTotal == 0 || nonOwnerReviewed >= nonOwnerTotal) {
            session.setPhase(SwotPhase.FINALIZING);
            session.setReviewLockedAt(LocalDateTime.now());
            swotSessionRepository.save(session);
            auditService.log(participant.getUser(), "SWOT_REVIEW_LOCKED", "SwotSession", session.getId(),
                    session.getStrategy(), "All non-owner participants reviewed; owner finalization is now open");
        }
    }

    @Transactional(readOnly = true)
    public SwotReviewSummaryResponse getReviewSummary(Long strategyId) {
        SwotSession session = requireSession(strategyId);
        List<SwotAlternativeProposalResponse> alternatives = swotAlternativeProposalRepository
                .findBySwotSessionIdOrderByCreatedAt(session.getId())
                .stream().map(this::toAlternativeResponse).collect(Collectors.toList());
        List<SwotReviewItemResponse> reviewItems = swotReviewItemRepository
                .findBySwotSessionIdAndIsOwnerFinal(session.getId(), false)
                .stream().map(this::toItemResponse).collect(Collectors.toList());
        List<SwotReviewItemResponse> ownerFinal = swotReviewItemRepository
                .findBySwotSessionIdAndIsOwnerFinal(session.getId(), true)
                .stream().map(this::toItemResponse).collect(Collectors.toList());

        List<SwotSuggestedGoalAdditionResponse> goalAdditions = swotSuggestedGoalAdditionRepository
                .findBySwotSuggestion_SwotSession_IdOrderByCreatedAt(session.getId())
                .stream().map(this::toGoalAdditionResponse).collect(Collectors.toList());

        return SwotReviewSummaryResponse.builder()
                .suggestions(swotSuggestionService.getSuggestions(strategyId))
                .alternatives(alternatives)
                .reviewItems(reviewItems)
                .ownerFinalDecisions(ownerFinal)
                .goalAdditions(goalAdditions)
                .build();
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
            case GOAL_ADDITION -> throw new BusinessRuleException(
                    "Proposed goal additions are decided by the Owner at finalization, not reviewed here");
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

    private SwotAlternativeProposalResponse toAlternativeResponse(SwotAlternativeProposal p) {
        List<SwotAlternativeGoalResponse> goals = swotAlternativeProposedGoalRepository
                .findByAlternativeProposalIdOrderBySortOrder(p.getId()).stream()
                .map(g -> SwotAlternativeGoalResponse.builder()
                        .id(g.getId()).title(g.getTitle()).description(g.getDescription()).sortOrder(g.getSortOrder())
                        .build())
                .collect(Collectors.toList());
        return SwotAlternativeProposalResponse.builder()
                .id(p.getId())
                .proposedById(p.getProposedBy().getId())
                .proposedByName(p.getProposedBy().getFname() + " " + p.getProposedBy().getLname())
                .name(p.getName())
                .rationale(p.getRationale())
                .createdAt(p.getCreatedAt())
                .goals(goals)
                .build();
    }

    private SwotSuggestedGoalAdditionResponse toGoalAdditionResponse(SwotSuggestedGoalAddition a) {
        return SwotSuggestedGoalAdditionResponse.builder()
                .id(a.getId())
                .swotSuggestionId(a.getSwotSuggestion().getId())
                .proposedById(a.getProposedBy().getId())
                .proposedByName(a.getProposedBy().getFname() + " " + a.getProposedBy().getLname())
                .title(a.getTitle())
                .description(a.getDescription())
                .createdAt(a.getCreatedAt())
                .build();
    }

    private SwotReviewItemResponse toItemResponse(SwotReviewItem i) {
        return SwotReviewItemResponse.builder()
                .id(i.getId())
                .reviewerId(i.getReviewer().getId())
                .reviewerName(i.getReviewer().getFname() + " " + i.getReviewer().getLname())
                .targetType(i.getTargetType())
                .targetId(i.getTargetId())
                .actionType(i.getActionType())
                .editedTitle(i.getEditedTitle())
                .editedDescription(i.getEditedDescription())
                .ownerFinal(Boolean.TRUE.equals(i.getIsOwnerFinal()))
                .build();
    }

    private SwotSession requireSession(Long strategyId) {
        return swotSessionRepository.findByStrategyId(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("SwotSession for strategy", strategyId));
    }
}
