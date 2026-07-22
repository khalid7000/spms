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
    AI_ENGINE_DOWN,
    /** entityId = null -- system-level alert (GatewaySsoAuthenticationFilter): a gateway-SSO
     *  authenticated user (identity already verified by the third-party gateway) has no
     *  corresponding AppUser in this deployment. Message text carries the identity/details an
     *  Admin needs to decide whether to provision the account. */
    GATEWAY_SSO_UNPROVISIONED,
    /** entityId = ImprovementTask id -- recipient is the VSM map's author, someone pulled a task
     *  off their board */
    VSM_TASK_PULLED,
    /** entityId = ImprovementTask id -- recipient is the VSM map's author, a pulled task was
     *  marked done */
    VSM_TASK_COMPLETED,
    /** entityId = VsmAuthorGrant id -- recipient is the top-of-hierarchy required approver */
    VSM_AUTHOR_GRANT_APPROVAL,
    /** entityId = VsmAuthorGrant id -- recipient is the delegated employee, message carries the
     *  approve/reject outcome */
    VSM_AUTHOR_GRANT_DECIDED,
    /** entityId = ApprovalDelegation id -- recipient is the delegator's own manager, whose sign-off
     *  is required because the delegate is neither an ancestor head nor a direct report */
    APPROVAL_DELEGATION_MANAGER_APPROVAL,
    /** entityId = ApprovalDelegation id -- recipient is the delegator, message carries the
     *  approve/reject outcome from their manager */
    APPROVAL_DELEGATION_DECIDED,
    /** entityId = ApprovalDelegation id -- recipient is the delegate, told that approval authority
     *  has been handed to them for a date range */
    APPROVAL_DELEGATION_ACTIVATED,
    /** entityId = ImprovementTask id -- recipient is the employee who had pulled it, told the map's
     *  author returned it to the board (now AVAILABLE again) before they completed it */
    VSM_TASK_RETURNED_TO_BOARD,
    /** entityId = ImprovementTask id -- recipient is the employee just added as a collaborator on
     *  someone else's pulled task */
    VSM_TASK_ASSIGNED
}
