package com.rit.spms.service;

import com.rit.spms.domain.AppUser;
import com.rit.spms.domain.enums.NotificationType;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Bridges the employee goal-setting workflow to {@link NotificationService}. AFTER_COMMIT, same
 * reasoning as {@link StrategyNotificationTrigger}: only notify once the submission has actually
 * persisted.
 */
@Component
@RequiredArgsConstructor
public class GoalCycleNotificationTrigger {

    private final AppUserRepository userRepository;
    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGoalCycleSubmitted(GoalCycleSubmittedEvent event) {
        AppUser employee = requireUser(event.employeeId());
        AppUser leader = requireUser(event.leaderId());
        notificationService.create(employee,
                "Your department head, " + fullName(leader) + ", submitted goals for you to review.",
                NotificationType.GOAL_CYCLE, event.cycleId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGoalCycleSentBack(GoalCycleSentBackEvent event) {
        AppUser leader = requireUser(event.leaderId());
        AppUser employee = requireUser(event.employeeId());
        notificationService.create(leader,
                fullName(employee) + " sent their goals back for more consideration.",
                NotificationType.GOAL_CYCLE, event.cycleId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGoalCycleDeployed(GoalCycleDeployedEvent event) {
        AppUser leader = requireUser(event.leaderId());
        AppUser employee = requireUser(event.employeeId());
        notificationService.create(leader,
                fullName(employee) + " accepted and signed their goals -- the goal cycle is now deployed for the year.",
                NotificationType.GOAL_CYCLE, event.cycleId());
    }

    private AppUser requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", userId));
    }

    private String fullName(AppUser user) {
        return user.getFname() + " " + user.getLname();
    }
}
