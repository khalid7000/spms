package com.rit.spms.domain.enums;

/**
 * What a {@link com.rit.spms.domain.Notification}'s {@code entityId} points at, so the frontend
 * knows where to route on click. Extend this enum as new notification-producing flows are added.
 */
public enum NotificationType {
    /** entityId = AnnualEvaluation id */
    ANNUAL_EVALUATION,
    /** entityId = Strategy id */
    STRATEGY_MEMBERSHIP,
    /** entityId = Strategy id */
    STRATEGY_APPROVAL,
    /** entityId = Strategy id */
    SWOT_INVITE,
    /** entityId = EmployeeGoalCycle id */
    GOAL_CYCLE
}
