package com.rit.spms.service;

import com.rit.spms.domain.AppUser;
import com.rit.spms.domain.Strategy;
import com.rit.spms.domain.enums.NotificationType;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.repository.AppUserRepository;
import com.rit.spms.repository.StrategyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Bridges strategy-membership, approval, and SWOT-invite events to {@link NotificationService}.
 * AFTER_COMMIT, same reasoning as {@link AnnualEvaluationEventTrigger}: only notify once the
 * triggering change has actually persisted.
 */
@Component
@RequiredArgsConstructor
public class StrategyNotificationTrigger {

    private final StrategyRepository strategyRepository;
    private final AppUserRepository userRepository;
    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMemberAdded(StrategyMemberAddedEvent event) {
        Strategy strategy = requireStrategy(event.strategyId());
        AppUser user = requireUser(event.userId());
        notificationService.create(user,
                "You were added to strategy '" + strategy.getTitle() + "' as " + event.role() + ".",
                NotificationType.STRATEGY_MEMBERSHIP, strategy.getId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApprovalPending(StrategyApprovalPendingEvent event) {
        Strategy strategy = requireStrategy(event.strategyId());
        AppUser approver = requireUser(event.approverId());
        notificationService.create(approver,
                "Strategy '" + strategy.getTitle() + "' is awaiting your approval for deployment.",
                NotificationType.STRATEGY_APPROVAL, strategy.getId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSwotInvite(SwotInviteEvent event) {
        Strategy strategy = requireStrategy(event.strategyId());
        AppUser user = requireUser(event.userId());
        notificationService.create(user,
                "You've been invited to participate in the SWOT analysis for strategy '" + strategy.getTitle() + "'.",
                NotificationType.SWOT_INVITE, strategy.getId());
    }

    private Strategy requireStrategy(Long strategyId) {
        return strategyRepository.findById(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy", strategyId));
    }

    private AppUser requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", userId));
    }
}
