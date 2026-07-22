package com.rit.spms.service;

/** Published when a new ApprovalDelegation needs the delegator's manager to sign off before it
 *  activates (the delegate is neither an ancestor head nor a direct report). */
public record ApprovalDelegationPendingEvent(Long delegationId) {
}
