package com.rit.spms.service;

import com.rit.spms.domain.ApprovalDelegation;
import com.rit.spms.domain.enums.ApprovalDelegationStatus;
import com.rit.spms.domain.enums.NotificationType;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.repository.ApprovalDelegationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Bridges ApprovalDelegation lifecycle events to {@link NotificationService}. AFTER_COMMIT, same
 *  reasoning as {@link VsmNotificationTrigger}: only notify once the triggering change has
 *  actually persisted. */
@Component
@RequiredArgsConstructor
public class ApprovalDelegationNotificationTrigger {

    private final ApprovalDelegationRepository approvalDelegationRepository;
    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPending(ApprovalDelegationPendingEvent event) {
        ApprovalDelegation d = require(event.delegationId());
        notificationService.create(d.getManagerApprover(),
                d.getDelegator().getFname() + " " + d.getDelegator().getLname()
                        + " wants to delegate their approval authority for '" + scopeName(d)
                        + "' to " + d.getDelegate().getFname() + " " + d.getDelegate().getLname()
                        + " and needs your approval.",
                NotificationType.APPROVAL_DELEGATION_MANAGER_APPROVAL, d.getId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDecided(ApprovalDelegationDecidedEvent event) {
        ApprovalDelegation d = require(event.delegationId());
        String outcome = d.getStatus() == ApprovalDelegationStatus.ACTIVE ? "approved" : "rejected";
        notificationService.create(d.getDelegator(),
                "Your delegation of '" + scopeName(d) + "' approval authority to "
                        + d.getDelegate().getFname() + " " + d.getDelegate().getLname()
                        + " was " + outcome + " by your manager.",
                NotificationType.APPROVAL_DELEGATION_DECIDED, d.getId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onActivated(ApprovalDelegationActivatedEvent event) {
        ApprovalDelegation d = require(event.delegationId());
        notificationService.create(d.getDelegate(),
                d.getDelegator().getFname() + " " + d.getDelegator().getLname()
                        + " has delegated their '" + scopeName(d) + "' approval authority to you from "
                        + d.getStartDate() + " to " + d.getEndDate() + ".",
                NotificationType.APPROVAL_DELEGATION_ACTIVATED, d.getId());
    }

    private String scopeName(ApprovalDelegation d) {
        return d.getDepartment() != null ? d.getDepartment().getName() : d.getOrgGroup().getTitle();
    }

    private ApprovalDelegation require(Long id) {
        return approvalDelegationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ApprovalDelegation", id));
    }
}
