package com.rit.spms.domain.enums;

/** Whether an {@link com.rit.spms.domain.ApprovalDelegation}'s delegated authority is anchored to a
 *  single Department headship or an OrgGroup headship. Deliberately separate from {@link
 *  VsmScopeType} even though the shape is identical -- that enum's contract is VSM-specific. */
public enum DelegationScopeType {
    DEPARTMENT,
    ORG_GROUP
}
