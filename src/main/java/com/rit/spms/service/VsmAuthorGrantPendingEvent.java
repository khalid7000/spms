package com.rit.spms.service;

/** Published when an Admin creates a VSM author grant; notifies the required top-of-hierarchy approver. */
public record VsmAuthorGrantPendingEvent(Long grantId) {
}
