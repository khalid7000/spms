package com.rit.spms.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;

/**
 * Creates LDAP beans only when ldap.enabled=true.
 * When disabled (the default), Spring does not attempt any LDAP connection.
 */
@Configuration
@ConditionalOnProperty(name = "ldap.enabled", havingValue = "true")
@RequiredArgsConstructor
public class LdapAuthConfig {

    private final LdapProperties ldap;

    @Bean
    public LdapContextSource ldapContextSource() {
        LdapContextSource source = new LdapContextSource();
        source.setUrl(ldap.getUrl());
        if (!ldap.getBase().isBlank()) {
            source.setBase(ldap.getBase());
        }
        if (!ldap.getBindDn().isBlank()) {
            source.setUserDn(ldap.getBindDn());
            source.setPassword(ldap.getBindPassword());
        }
        return source;
    }

    @Bean
    public LdapAuthenticator ldapAuthenticator(LdapContextSource contextSource) {
        BindAuthenticator authenticator = new BindAuthenticator(contextSource);

        if (!ldap.getUserSearchFilter().isBlank()) {
            // Search-based (e.g. Active Directory): find the user's DN via a filter first,
            // then bind with the found DN + supplied password.
            FilterBasedLdapUserSearch search = new FilterBasedLdapUserSearch(
                    ldap.getUserSearchBase(),
                    ldap.getUserSearchFilter(),
                    contextSource);
            authenticator.setUserSearch(search);
        } else {
            // Pattern-based: construct the DN directly from the username.
            authenticator.setUserDnPatterns(new String[]{ ldap.getUserDnPattern() });
        }
        return authenticator;
    }
}
