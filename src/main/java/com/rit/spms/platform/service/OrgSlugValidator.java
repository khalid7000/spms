package com.rit.spms.platform.service;

import com.rit.spms.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Slug/schema-name rules shared by every path that registers an organization (the
 * one-time existing-schema import and, later, the full create-from-scratch flow) --
 * both need the same guarantees before a slug or schema name is ever stored or used in
 * a DDL statement: URL-safe, not colliding with a reserved application path, and safe to
 * splice into a CREATE SCHEMA/search_path statement (Postgres identifiers can't be
 * parameterized like ordinary query values).
 */
@Component
public class OrgSlugValidator {

    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]([a-z0-9-]{0,48}[a-z0-9])?$");

    private static final Pattern SCHEMA_NAME_PATTERN = Pattern.compile("^[a-z][a-z0-9_]{0,62}$");

    private static final Set<String> RESERVED_SLUGS = Set.of(
            "api", "admin", "assets", "static", "login", "console", "platform",
            "super-admin", "uploads", "www", "app", "favicon.ico", "robots.txt",
            "actuator", "swagger-ui", "v3"
    );

    public void validateFormat(String slug) {
        if (slug == null || !SLUG_PATTERN.matcher(slug).matches()) {
            throw new BusinessRuleException(
                    "Slug must be lowercase alphanumeric with optional hyphens, 1-50 characters.");
        }
        if (RESERVED_SLUGS.contains(slug)) {
            throw new BusinessRuleException(
                    "'" + slug + "' is a reserved path and can't be used as an organization slug.");
        }
    }

    /** Derives a schema name from a slug and re-validates it as a safe Postgres identifier
     * -- this re-validated string is the only thing ever spliced into a DDL statement. */
    public String deriveSchemaName(String slug) {
        String schemaName = "org_" + slug.replace('-', '_');
        if (!SCHEMA_NAME_PATTERN.matcher(schemaName).matches()) {
            throw new BusinessRuleException("Derived schema name is invalid: " + schemaName);
        }
        return schemaName;
    }
}
