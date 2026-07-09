package com.rit.spms.service;

/** Published once the head submits their evaluation, entering the signature window; notifies the employee. */
public record AnnualEvaluationHeadReadyEvent(Long evaluationId) {
}
