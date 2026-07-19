package com.rit.spms.platform.domain;

import com.rit.spms.platform.domain.enums.AuthMode;
import com.rit.spms.platform.domain.enums.OrgStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * A tenant registered on the platform. Each row points at its own Postgres schema
 * ({@link #schemaName}) holding a full, independent copy of the tenant table set --
 * isolation is structural (separate schema), not a filtered column, so this entity never
 * joins across to any tenant's data. Always queried through the fixed "platform" schema
 * regardless of which tenant a request is currently scoped to.
 */
@Entity
@Table(name = "organization", schema = "platform")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String slug;

    @Column(name = "schema_name", nullable = false, unique = true, length = 63)
    private String schemaName;

    /** At most one organization may serve at the bare domain root (no /slug prefix). */
    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    @Column(name = "logo_path", length = 500)
    private String logoPath;

    /** Comma-separated extra Flyway "classpath:" locations replayed into this org's schema in
     * addition to the generic tenant baseline (db/migration) -- see the migration that adds
     * this column for why. Null for every org except org_rit. */
    @Column(name = "extra_content_locations", length = 500)
    private String extraContentLocations;

    @Column(length = 500)
    private String address;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrgStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_mode", nullable = false, length = 20)
    @Builder.Default
    private AuthMode authMode = AuthMode.LOCAL;

    @Column(name = "ldap_url", length = 300)
    private String ldapUrl;

    @Column(name = "ldap_base", length = 300)
    private String ldapBase;

    @Column(name = "ldap_user_dn_pattern", length = 300)
    private String ldapUserDnPattern;

    @Column(name = "ldap_user_search_filter", length = 300)
    private String ldapUserSearchFilter;

    @Column(name = "ldap_user_search_base", length = 300)
    private String ldapUserSearchBase;

    @Column(name = "ldap_bind_dn", length = 300)
    private String ldapBindDn;

    @Column(name = "ldap_bind_password", length = 300)
    private String ldapBindPassword;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
