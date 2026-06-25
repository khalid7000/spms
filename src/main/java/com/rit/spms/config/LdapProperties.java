package com.rit.spms.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * LDAP connection settings.
 * Set ldap.enabled=true in application.yml (or an environment-specific override)
 * to activate LDAP authentication at deployment time — no code changes required.
 */
@Component
@ConfigurationProperties(prefix = "ldap")
@Getter
@Setter
public class LdapProperties {

    /** Set to true to authenticate users against the LDAP directory. */
    private boolean enabled = false;

    /** LDAP server URL, e.g. ldap://ldap.rit.edu:389 or ldaps://ldap.rit.edu:636 */
    private String url = "";

    /** Base DN for all LDAP operations, e.g. dc=rit,dc=edu */
    private String base = "";

    /**
     * DN pattern used to construct the user's distinguished name.
     * {0} is replaced by the username (email). Example: uid={0},ou=people
     * Leave blank if using search-based auth (set userSearchFilter instead).
     */
    private String userDnPattern = "uid={0},ou=people";

    /**
     * LDAP search filter for search-based authentication (e.g. Active Directory).
     * Example: (sAMAccountName={0})  or  (userPrincipalName={0})
     * Leave blank to use userDnPattern instead.
     */
    private String userSearchFilter = "";

    /** Search base for user lookups when using userSearchFilter. */
    private String userSearchBase = "";

    /** Manager/bind DN used to search the directory (leave blank for anonymous bind). */
    private String bindDn = "";

    /** Manager/bind password. */
    private String bindPassword = "";
}
