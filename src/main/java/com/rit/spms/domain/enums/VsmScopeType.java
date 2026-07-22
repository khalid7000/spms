package com.rit.spms.domain.enums;

/** Whether a {@link com.rit.spms.domain.VsmMap} (or a future VsmAuthorGrant) is scoped to a single
 *  Department or to an OrgGroup (and everything under it). */
public enum VsmScopeType {
    DEPARTMENT,
    ORG_GROUP
}
