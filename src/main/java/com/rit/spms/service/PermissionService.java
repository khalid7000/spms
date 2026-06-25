package com.rit.spms.service;

import com.rit.spms.domain.Strategy;
import com.rit.spms.domain.enums.RoleType;
import com.rit.spms.domain.enums.StrategyState;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.exception.UnauthorizedException;
import com.rit.spms.repository.RoleAssignmentRepository;
import com.rit.spms.repository.StrategyApprovalRepository;
import com.rit.spms.repository.StrategyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final RoleAssignmentRepository roleAssignmentRepository;
    private final StrategyRepository strategyRepository;
    private final StrategyApprovalRepository strategyApprovalRepository;

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
        if (state == StrategyState.DEPLOYED || state == StrategyState.APPROVAL_PENDING) return false;
        if (state == StrategyState.FROZEN) return role == RoleType.OWNER;
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
        if (state == StrategyState.REVIEW || state == StrategyState.APPROVAL_PENDING) {
            return false;
        }
        if (state == StrategyState.DEPLOYED || state == StrategyState.FROZEN) {
            return isOwner(userId, strategyId);
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

        if (strategy.getState() == StrategyState.FROZEN && !isOwner(userId, strategyId)) {
            throw new UnauthorizedException("Only the Owner can mutate a FROZEN strategy");
        }

        if (strategy.getState() == StrategyState.DEPLOYED) {
            throw new UnauthorizedException("Plan content cannot be edited in DEPLOYED state");
        }

        if (strategy.getState() == StrategyState.APPROVAL_PENDING) {
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
}
