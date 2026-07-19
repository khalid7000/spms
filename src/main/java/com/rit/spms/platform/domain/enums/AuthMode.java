package com.rit.spms.platform.domain.enums;

/** How an organization's users authenticate. LDAP wiring is per-org config only for now --
 * the dynamic per-tenant bind logic is a follow-up (see the multi-tenancy plan's Phase 6). */
public enum AuthMode {
    LOCAL,
    LDAP
}
