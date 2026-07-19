package com.rit.spms.platform.service;

import com.rit.spms.exception.BusinessRuleException;
import com.rit.spms.platform.domain.Organization;
import com.rit.spms.platform.domain.enums.AuthMode;
import com.rit.spms.platform.domain.enums.OrgStatus;
import com.rit.spms.platform.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Onboards organizations onto the platform.
 *
 * <p>{@link #importExisting} registers a schema that already exists and has already been
 * fully migrated outside of this flow. It exists for exactly one purpose: the RIT
 * cutover -- an operator renames RIT's current default schema to a proper tenant schema
 * name (a schema-only rename, no row copy) and then calls this once to register it. This
 * is deliberately never invoked automatically at startup: a fresh, client-only deployment
 * never has RIT data to import, and its {@code organization} table simply starts empty --
 * that client's own org becomes the first (and, if flagged {@code isDefault}, the one
 * served at that deployment's bare domain root) via the normal create-from-scratch flow.
 *
 * <p>The full create-from-scratch flow (schema creation + Flyway baseline against the new
 * schema + admin user seed) lands in a later phase; only the import path is needed to
 * unblock the RIT cutover and to prove the registry/entities work end-to-end.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationProvisioningService {

    private final OrganizationRepository organizationRepository;
    private final OrgSlugValidator slugValidator;
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final TenantAdminSeeder tenantAdminSeeder;

    @Value("${app.uploads.dir}")
    private String uploadsDir;

    private static final String TENANT_BASELINE_LOCATION = "classpath:db/migration";

    /**
     * Creates a brand-new organization from scratch: schema + tenant baseline migrations +
     * initial Admin user + (optional) logo. Deliberately NOT one JPA transaction -- CREATE
     * SCHEMA, Flyway's own internal transaction management, and a schema-switched insert
     * can't coherently share one. On failure partway through, the row is left {@code FAILED}
     * for an operator to inspect/retry rather than silently rolled back.
     */
    public Organization createOrganization(String name, String slug, String address, String description,
                                            boolean isDefault, String adminEmail, String adminPassword,
                                            MultipartFile logo) {
        slugValidator.validateFormat(slug);
        if (organizationRepository.existsBySlug(slug)) {
            throw new BusinessRuleException("An organization with slug '" + slug + "' already exists.");
        }
        if (isDefault && organizationRepository.findByIsDefaultTrue().isPresent()) {
            throw new BusinessRuleException(
                    "An organization is already registered as the default (root-path) organization.");
        }

        String schemaName = slugValidator.deriveSchemaName(slug);
        if (organizationRepository.existsBySchemaName(schemaName)) {
            throw new BusinessRuleException(
                    "Schema '" + schemaName + "' is already registered to an organization.");
        }

        Organization organization = Organization.builder()
                .name(name)
                .slug(slug)
                .schemaName(schemaName)
                .isDefault(isDefault)
                .address(address)
                .description(description)
                .status(OrgStatus.PROVISIONING)
                .authMode(AuthMode.LOCAL)
                .build();
        organization = organizationRepository.save(organization);

        try {
            jdbcTemplate.execute("CREATE SCHEMA \"" + schemaName + "\"");

            Flyway.configure()
                    .dataSource(dataSource)
                    .schemas(schemaName)
                    .locations(TENANT_BASELINE_LOCATION)
                    .load()
                    .migrate();

            tenantAdminSeeder.seedInitialAdmin(schemaName, adminEmail, adminPassword);

            if (logo != null && !logo.isEmpty()) {
                organization.setLogoPath(storeLogo(schemaName, logo));
            }

            organization.setStatus(OrgStatus.ACTIVE);
            return organizationRepository.save(organization);
        } catch (Exception e) {
            log.error("Provisioning failed for organization '{}' (schema '{}')", slug, schemaName, e);
            organization.setStatus(OrgStatus.FAILED);
            organizationRepository.save(organization);
            throw new BusinessRuleException("Failed to provision organization: " + e.getMessage());
        }
    }

    private String storeLogo(String schemaName, MultipartFile logo) {
        try {
            String originalName = logo.getOriginalFilename();
            String extension = (originalName != null && originalName.contains("."))
                    ? originalName.substring(originalName.lastIndexOf('.'))
                    : "";
            if (!extension.matches("(?i)\\.(png|jpg|jpeg|gif|svg|webp)")) {
                throw new BusinessRuleException("Logo must be an image file (png, jpg, jpeg, gif, svg, webp).");
            }

            Path orgDir = Path.of(uploadsDir, "orgs", schemaName);
            Files.createDirectories(orgDir);
            Path target = orgDir.resolve("logo" + extension);
            logo.transferTo(target);

            return "/uploads/orgs/" + schemaName + "/logo" + extension;
        } catch (IOException e) {
            throw new BusinessRuleException("Failed to store logo: " + e.getMessage());
        }
    }

    @Transactional
    public Organization importExisting(String schemaName, String slug, String name,
                                        String address, String description, boolean isDefault) {
        slugValidator.validateFormat(slug);

        if (organizationRepository.existsBySlug(slug)) {
            throw new BusinessRuleException("An organization with slug '" + slug + "' already exists.");
        }
        if (organizationRepository.existsBySchemaName(schemaName)) {
            throw new BusinessRuleException(
                    "Schema '" + schemaName + "' is already registered to an organization.");
        }
        if (!schemaExists(schemaName)) {
            throw new BusinessRuleException(
                    "Schema '" + schemaName + "' does not exist -- create or rename it before importing.");
        }
        if (isDefault && organizationRepository.findByIsDefaultTrue().isPresent()) {
            throw new BusinessRuleException(
                    "An organization is already registered as the default (root-path) organization.");
        }

        Organization organization = Organization.builder()
                .name(name)
                .slug(slug)
                .schemaName(schemaName)
                .isDefault(isDefault)
                .address(address)
                .description(description)
                .status(OrgStatus.ACTIVE)
                .authMode(AuthMode.LOCAL)
                .build();

        return organizationRepository.save(organization);
    }

    private boolean schemaExists(String schemaName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = ?",
                Integer.class, schemaName);
        return count != null && count > 0;
    }
}
