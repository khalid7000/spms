package com.rit.spms.platform.domain.enums;

/** Lifecycle of an organization's provisioning. FAILED is left in place (not auto-cleaned)
 * so an operator can inspect what happened before retrying or deleting. */
public enum OrgStatus {
    PROVISIONING,
    ACTIVE,
    SUSPENDED,
    FAILED
}
