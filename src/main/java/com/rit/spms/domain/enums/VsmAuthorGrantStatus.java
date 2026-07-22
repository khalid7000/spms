package com.rit.spms.domain.enums;

/** Lifecycle of a {@link com.rit.spms.domain.VsmAuthorGrant} -- an Admin-initiated delegation of
 *  "VSM author" rights over a unit to an employee who isn't its head, requiring sign-off from the
 *  top-of-hierarchy head above that employee before it takes effect. */
public enum VsmAuthorGrantStatus {
    PENDING_APPROVAL,
    ACTIVE,
    REJECTED,
    REVOKED
}
