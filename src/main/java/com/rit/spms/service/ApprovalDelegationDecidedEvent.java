package com.rit.spms.service;

/** Published when the delegator's manager approves or rejects a pending ApprovalDelegation;
 *  notifies the delegator of the outcome. */
public record ApprovalDelegationDecidedEvent(Long delegationId) {
}
