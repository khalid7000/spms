package com.rit.spms.service;

/** Published when an ApprovalDelegation becomes ACTIVE (immediately at creation for an ancestor
 *  head/direct report or a top-of-pyramid delegator, or once the manager approves); notifies the
 *  delegate that they now hold this approval authority. */
public record ApprovalDelegationActivatedEvent(Long delegationId) {
}
