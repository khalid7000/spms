package com.rit.spms.platform.service;

import com.rit.spms.config.LdapProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Seeds the initial Admin user for a newly-provisioned org, via plain JDBC rather than
 * {@code AppUserRepository}/Hibernate.
 *
 * <p>This isn't a style preference -- a Hibernate/JPA-based insert was tried first, using
 * {@link com.rit.spms.platform.tenant.TenantContext} plus a separate
 * {@code @Transactional(propagation = REQUIRES_NEW)} bean (to force a fresh Hibernate Session
 * so {@code CurrentTenantIdentifierResolver} would re-resolve against the new org's schema
 * rather than reusing whatever tenant the request's {@code spring.jpa.open-in-view} session
 * had already resolved to at its first DB touch). Verified against a real provisioning run
 * that it does NOT work as the textbook Spring transaction-propagation model suggests: the
 * insert still landed in the default schema, meaning no fresh connection was actually
 * acquired through {@code SchemaMultiTenantConnectionProvider} for the new transaction. Since
 * getting this wrong means an org's very first Admin account silently doesn't exist where
 * expected, plain schema-qualified JDBC -- which never touches Hibernate's multi-tenancy
 * routing at all -- is the safe choice here, matching how {@code CREATE SCHEMA} itself is
 * already done in {@code OrganizationProvisioningService}.
 *
 * <p>Trade-off: this duplicates app_user/app_user_system_role's shape rather than reusing the
 * JPA entity, so a future migration adding a new NOT NULL column to app_user needs this
 * updated too.
 */
@Service
@RequiredArgsConstructor
public class TenantAdminSeeder {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final LdapProperties ldapProperties;

    public void seedInitialAdmin(String schemaName, String adminEmail, String adminPassword) {
        // Forcing a password change makes no sense (and is a dead end -- AuthController
        // rejects it outright) when LDAP owns the password. Same rule the retired
        // DataInitializer used to apply.
        boolean mustChangePassword = !ldapProperties.isEnabled();
        Long userId = jdbcTemplate.queryForObject(
                "INSERT INTO \"" + schemaName + "\".app_user "
                        + "(fname, lname, email, password_hash, active, must_change_password) "
                        + "VALUES (?, ?, ?, ?, true, ?) RETURNING id",
                Long.class, "Admin", "User", adminEmail, passwordEncoder.encode(adminPassword), mustChangePassword);

        jdbcTemplate.update(
                "INSERT INTO \"" + schemaName + "\".app_user_system_role (user_id, role) VALUES (?, 'ADMIN')",
                userId);
    }
}
