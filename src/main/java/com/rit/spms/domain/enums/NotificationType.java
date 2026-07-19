package com.rit.spms.domain.enums;

/**
 * What a {@link com.rit.spms.domain.Notification}'s {@code entityId} points at, so the frontend
 * knows where to route on click. Extend this enum as new notification-producing flows are added.
 */
public enum NotificationType {
    /** entityId = AnnualEvaluation id -- recipient is the employee, route to their own evaluation */
    ANNUAL_EVALUATION,
    /** entityId = AnnualEvaluation id -- recipient is the head, route to Team Evaluations */
    ANNUAL_EVALUATION_HEAD,
    /** entityId = Strategy id */
    STRATEGY_MEMBERSHIP,
    /** entityId = Strategy id */
    STRATEGY_APPROVAL,
    /** entityId = Strategy id */
    SWOT_INVITE,
    /** entityId = EmployeeGoalCycle id -- recipient is the employee, route to their own goals */
    GOAL_CYCLE,
    /** entityId = EmployeeGoalCycle id -- recipient is the leader/head, route to Team Goals */
    GOAL_CYCLE_HEAD,
    /** entityId = null -- system-level alert (AiEngineHealthCheckService), not tied to any
     *  business entity; the frontend has no click-routing case for it and that's fine, it just
     *  marks read like any other notification. Message text distinguishes down vs. recovered. */
    AI_ENGINE_DOWN
}
