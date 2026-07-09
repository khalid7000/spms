package com.rit.spms.service;

/** Published each time the head edits a rank after their initial submission (before anyone signs); notifies the employee. */
public record AnnualEvaluationEditedEvent(Long evaluationId) {
}
