package com.rit.spms.service;

import com.rit.spms.domain.Strategy;
import com.rit.spms.domain.SwotParticipant;
import com.rit.spms.domain.SwotSession;
import com.rit.spms.domain.enums.RoleType;
import com.rit.spms.domain.enums.StrategyState;
import com.rit.spms.domain.enums.SwotPhase;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.exception.UnauthorizedException;
import com.rit.spms.repository.RoleAssignmentRepository;
import com.rit.spms.repository.StrategyApprovalRepository;
import com.rit.spms.repository.StrategyRepository;
import com.rit.spms.repository.SwotParticipantRepository;
import com.rit.spms.repository.SwotSessionRepository;
import com.rit.spms.repository.VisionAreaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final RoleAssignmentRepository roleAssignmentRepository;
    private final StrategyRepository strategyRepository;
    private final StrategyApprovalRepository strategyApprovalRepository;
    private final SwotSessionRepository swotSessionRepository;
    private final SwotParticipantRepository swotParticipantRepository;
    private final VisionAreaRepository visionAreaRepository;

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
