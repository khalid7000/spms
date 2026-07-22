package com.rit.spms.domain.enums;

/** Lifecycle of an {@link com.rit.spms.domain.ApprovalDelegation}. Delegating to an ancestor head or
 *  a direct report activates immediately (no separate status needed for that distinction --
 *  {@code requiresManagerApproval} on the entity records which path was taken); delegating to
 *  anyone else needs the delegator's own manager to sign off first, unless the delegator is at the
 *  top of the org pyramid (no manager to ask), in which case it also activates immediately. */
public enum ApprovalDelegationStatus {
    PENDING_MANAGER_APPROVAL,
    ACTIVE,
    REJECTED,
    CANCELLED
}
