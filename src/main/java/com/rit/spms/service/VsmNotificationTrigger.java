package com.rit.spms.service;

import com.rit.spms.domain.AppUser;
import com.rit.spms.domain.ImprovementTask;
import com.rit.spms.domain.VsmAuthorGrant;
import com.rit.spms.domain.enums.NotificationType;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.repository.AppUserRepository;
import com.rit.spms.repository.ImprovementTaskRepository;
import com.rit.spms.repository.VsmAuthorGrantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Bridges VSM Kanban task events and author-grant events to {@link NotificationService}.
 *  AFTER_COMMIT, same reasoning as {@link StrategyNotificationTrigger}/{@link
 *  AnnualEvaluationEventTrigger}: only notify once the triggering change has actually persisted. */
@Component
@RequiredArgsConstructor
public class VsmNotificationTrigger {

    private final ImprovementTaskRepository improvementTaskRepository;
    private final VsmAuthorGrantRepository vsmAuthorGrantRepository;
    private final AppUserRepository appUserRepository;
    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTaskPulled(VsmTaskPulledEvent event) {
        ImprovementTask task = requireTask(event.taskId());
        AppUser author = task.getKaizenNode().getVsmMap().getCreatedBy();
        notificationService.create(author,
                task.getPulledBy().getFname() + " " + task.getPulledBy().getLname()
                        + " pulled task '" + task.getTitle() + "' on your map '"
                        + task.getKaizenNode().getVsmMap().getTitle() + "'.",
                NotificationType.VSM_TASK_PULLED, task.getId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTaskCompleted(VsmTaskCompletedEvent event) {
        ImprovementTask task = requireTask(event.taskId());
        AppUser author = task.getKaizenNode().getVsmMap().getCreatedBy();
        notificationService.create(author,
                "Task '" + task.getTitle() + "' on your map '" + task.getKaizenNode().getVsmMap().getTitle()
                        + "' was marked done.",
                NotificationType.VSM_TASK_COMPLETED, task.getId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTaskReturnedToBoard(VsmTaskReturnedToBoardEvent event) {
        ImprovementTask task = requireTask(event.taskId());
        com.rit.spms.domain.AppUser previousPuller = appUserRepository.findById(event.previousPullerId())
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", event.previousPullerId()));
        notificationService.create(previousPuller,
                "Task '" + task.getTitle() + "' on map '" + task.getKaizenNode().getVsmMap().getTitle()
                        + "' was returned to the board by the map's author.",
                NotificationType.VSM_TASK_RETURNED_TO_BOARD, task.getId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTaskAssigned(VsmTaskAssignedEvent event) {
        ImprovementTask task = requireTask(event.taskId());
        com.rit.spms.domain.AppUser employee = appUserRepository.findById(event.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", event.employeeId()));
        notificationService.create(employee,
                "You were added as a collaborator on task '" + task.getTitle() + "' on map '"
                        + task.getKaizenNode().getVsmMap().getTitle() + "'.",
                NotificationType.VSM_TASK_ASSIGNED, task.getId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAuthorGrantPending(VsmAuthorGrantPendingEvent event) {
        VsmAuthorGrant grant = requireGrant(event.grantId());
        String scopeName = grant.getDepartment() != null ? grant.getDepartment().getName() : grant.getOrgGroup().getTitle();
        notificationService.create(grant.getRequiredApprover(),
                grant.getEmployee().getFname() + " " + grant.getEmployee().getLname()
                        + " has been proposed as a VSM author for '" + scopeName + "' and needs your approval.",
                NotificationType.VSM_AUTHOR_GRANT_APPROVAL, grant.getId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAuthorGrantDecided(VsmAuthorGrantDecidedEvent event) {
        VsmAuthorGrant grant = requireGrant(event.grantId());
        String outcome = grant.getStatus() == com.rit.spms.domain.enums.VsmAuthorGrantStatus.ACTIVE ? "approved" : "rejected";
        notificationService.create(grant.getEmployee(),
                "Your VSM author request was " + outcome + " by " + grant.getApproverTitle() + ".",
                NotificationType.VSM_AUTHOR_GRANT_DECIDED, grant.getId());
    }

    private ImprovementTask requireTask(Long taskId) {
        return improvementTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("ImprovementTask", taskId));
    }

    private VsmAuthorGrant requireGrant(Long grantId) {
        return vsmAuthorGrantRepository.findById(grantId)
                .orElseThrow(() -> new ResourceNotFoundException("VsmAuthorGrant", grantId));
    }
}
