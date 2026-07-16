package com.rit.spms.service;

/** Published once the head sends the evaluation back to the employee for another review round; notifies the employee. */
public record AnnualEvaluationReturnedToEmployeeEvent(Long evaluationId) {
}
