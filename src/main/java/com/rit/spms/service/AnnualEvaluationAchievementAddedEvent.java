package com.rit.spms.service;

/** Published when a new achievement is logged against a portfolio while the evaluation is
 *  RETURNED_TO_EMPLOYEE; notifies the head that new content showed up during the revision round. */
public record AnnualEvaluationAchievementAddedEvent(Long evaluationId, String achievementTitle) {
}
