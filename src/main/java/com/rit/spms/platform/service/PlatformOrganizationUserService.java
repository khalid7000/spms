package com.rit.spms.platform.service;

import com.rit.spms.config.LdapProperties;
import com.rit.spms.exception.BusinessRuleException;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.platform.domain.Organization;
import com.rit.spms.platform.dto.response.TenantUserSummaryResponse;
import com.rit.spms.platform.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

/**
 * Lets a Super Admin see who's actually inside a given org's schema and, if the org's own
 * Admin forgets their password with no one else able to log in, recover access -- without
 * ever needing tenant-level credentials. Reads/writes go through plain JDBC against the
 * org's {@code schemaName} (never a request-supplied value -- always the one already stored
 * on the {@link Organization} row at provisioning time), the same safety rule
 * {@code PlatformDashboardService} and {@code TenantAdminSeeder} already follow.
 */
@Service
@RequiredArgsConstructor
public class PlatformOrganizationUserService {

    // Excludes visually-confusable characters (0/O, 1/l/I) since this is meant to be
    // read off-screen and retyped/copy-pasted by whoever the Super Admin hands it to.
    private static final String PASSWORD_CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final int GENERATED_PASSWORD_LENGTH = 12;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final OrganizationRepository organizationRepository;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final LdapProperties ldapProperties;

    public List<TenantUserSummaryResponse> listUsers(Long orgId) {
        Organization org = requireOrg(orgId);
        String schema = org.getSchemaName();
        return jdbcTemplate.query(
                "SELECT u.id, u.fname, u.lname, u.email, u.active, u.must_change_password, "
                        + "COALESCE(string_agg(r.role, ',' ORDER BY r.role), '') AS roles "
                        + "FROM \"" + schema + "\".app_user u "
                        + "LEFT JOIN \"" + schema + "\".app_user_system_role r ON r.user_id = u.id "
                        + "GROUP BY u.id, u.fname, u.lname, u.email, u.active, u.must_change_password "
                        + "ORDER BY u.id",
                (rs, rowNum) -> {
                    String roles = rs.getString("roles");
                    return TenantUserSummaryResponse.builder()
                            .id(rs.getLong("id"))
                            .fname(rs.getString("fname"))
                            .lname(rs.getString("lname"))
                            .email(rs.getString("email"))
                            .active(rs.getBoolean("active"))
                            .mustChangePassword(rs.getBoolean("must_change_password"))
                            .roles(roles == null || roles.isBlank() ? List.of() : Arrays.asList(roles.split(",")))
                            .build();
                });
    }

    /** Sets a fresh, randomly-generated password and forces a change on next login (same
     * flow a voluntary/forced self-service password change already goes through) -- returned
     * once so the Super Admin can hand it to whoever owns the account. Not applicable when
     * this deployment's authentication is LDAP-managed, same guard {@code AuthController}
     * already applies to self-service changes. */
    public String resetPassword(Long orgId, Long userId) {
        if (ldapProperties.isEnabled()) {
            throw new BusinessRuleException(
                    "Password resets are managed by the LDAP directory for this deployment. "
                    + "Please use your organisation's password reset portal.");
        }
        Organization org = requireOrg(orgId);
        String schema = org.getSchemaName();
        String newPassword = generatePassword();

        int updated = jdbcTemplate.update(
                "UPDATE \"" + schema + "\".app_user "
                        + "SET password_hash = ?, must_change_password = true "
                        + "WHERE id = ?",
                passwordEncoder.encode(newPassword), userId);
        if (updated == 0) {
            throw new ResourceNotFoundException("User not found in organization " + org.getName());
        }
        return newPassword;
    }

    private Organization requireOrg(Long orgId) {
        return organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + orgId));
    }

    private String generatePassword() {
        StringBuilder sb = new StringBuilder(GENERATED_PASSWORD_LENGTH);
        for (int i = 0; i < GENERATED_PASSWORD_LENGTH; i++) {
            sb.append(PASSWORD_CHARS.charAt(RANDOM.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }
}
