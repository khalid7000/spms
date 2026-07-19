-- Data-driven seam for orgs that need more than the generic tenant baseline (db/migration)
-- replayed into their schema -- e.g. org_rit also needs db/migration-rit-seed (RIT Dubai's
-- real plan content). Nullable/comma-separated Flyway "classpath:" locations; every migration
-- runner (OrganizationProvisioningService, TenantMigrationRunner) appends whatever this column
-- lists to the baseline. Left null for every org created from now on -- only org_rit's row
-- ever gets this set, and it's set as plain data, not a hardcoded name anywhere in Java code.
ALTER TABLE organization ADD COLUMN extra_content_locations VARCHAR(500);
