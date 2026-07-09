package com.rit.spms.service;

/** Published once the employee submits their self-assessment; notifies the head. */
public record AnnualEvaluationSubmittedEvent(Long evaluationId) {
}
