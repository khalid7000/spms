package com.rit.spms.service;

/** Published when a user becomes the current required approver in a strategy's deployment-approval chain; notifies them. */
public record StrategyApprovalPendingEvent(Long strategyId, Long approverId) {
}
