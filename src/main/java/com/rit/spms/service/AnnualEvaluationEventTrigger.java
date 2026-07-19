package com.rit.spms.service;

import com.rit.spms.domain.AnnualEvaluation;
import com.rit.spms.domain.enums.NotificationType;
import com.rit.spms.repository.AnnualEvaluationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Bridges the Annual Evaluation workflow's state-transition events to {@link NotificationService}.
 * AFTER_COMMIT so the notification is only created once the triggering change (submission, edit,
 * signature) has actually persisted -- a rollback of that transaction means no notification either.
 */
@Component
@RequiredArgsConstructor
public class AnnualEvaluationEventTrigger {

    private final AnnualEvaluationRepository evaluationRepository;
    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSubmitted(AnnualEvaluationSubmittedEvent event) {
        AnnualEvaluation evaluation = require(event.evaluationId());
        notificationService.create(evaluation.getHead(),
                evaluation.getEmployee().getFname() + " " + evaluation.getEmployee().getLname()
                        + " submitted their annual self-assessment for your review.",
                NotificationType.ANNUAL_EVALUATION_HEAD, evaluation.getId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onHeadReady(AnnualEvaluationHeadReadyEvent event) {
        AnnualEvaluation evaluation = require(event.evaluationId());
        notificationService.create(evaluation.getEmployee(),
                "Your annual evaluation is ready for signature.",
                NotificationType.ANNUAL_EVALUATION, evaluation.getId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReturnedToEmployee(AnnualEvaluationReturnedToEmployeeEvent event) {
        AnnualEvaluation evaluation = require(event.evaluationId());
        notificationService.create(evaluation.getEmployee(),
                "Your head returned your annual evaluation for another review -- please update and resubmit.",
                NotificationType.ANNUAL_EVALUATION, evaluation.getId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEdited(AnnualEvaluationEditedEvent event) {
        AnnualEvaluation evaluation = require(event.evaluationId());
        notificationService.create(evaluation.getEmployee(),
                "Your head updated your annual evaluation.",
                NotificationType.ANNUAL_EVALUATION, evaluation.getId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSigned(AnnualEvaluationSignedEvent event) {
        AnnualEvaluation evaluation = require(event.evaluationId());
        if (event.byHead()) {
            notificationService.create(evaluation.getEmployee(),
                    "Your head signed your annual evaluation.",
                    NotificationType.ANNUAL_EVALUATION, evaluation.getId());
        } else if (event.refused()) {
            notificationService.create(evaluation.getHead(),
                    evaluation.getEmployee().getFname() + " " + evaluation.getEmployee().getLname()
                            + " refused to sign their annual evaluation.",
                    NotificationType.ANNUAL_EVALUATION_HEAD, evaluation.getId());
        } else {
            notificationService.create(evaluation.getHead(),
                    evaluation.getEmployee().getFname() + " " + evaluation.getEmployee().getLname()
                            + " signed their annual evaluation.",
                    NotificationType.ANNUAL_EVALUATION_HEAD, evaluation.getId());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAchievementAddedWhileReturned(AnnualEvaluationAchievementAddedEvent event) {
        AnnualEvaluation evaluation = require(event.evaluationId());
        notificationService.create(evaluation.getHead(),
                evaluation.getEmployee().getFname() + " " + evaluation.getEmployee().getLname()
                        + " added a new achievement (\"" + event.achievementTitle()
                        + "\") to their returned annual evaluation.",
                NotificationType.ANNUAL_EVALUATION_HEAD, evaluation.getId());
    }

    private AnnualEvaluation require(Long evaluationId) {
        return evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new com.rit.spms.exception.ResourceNotFoundException("AnnualEvaluation", evaluationId));
    }
}
