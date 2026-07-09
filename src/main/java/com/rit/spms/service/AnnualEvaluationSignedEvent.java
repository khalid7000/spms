package com.rit.spms.service;

/** Published when either party signs or the employee refuses to sign; notifies the other party. */
public record AnnualEvaluationSignedEvent(Long evaluationId, boolean byHead, boolean refused) {
}
