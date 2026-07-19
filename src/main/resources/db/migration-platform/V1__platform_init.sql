-- Platform-tier registry: lives in its own "platform" schema, separate from every tenant
-- schema (each org gets its own full copy of db/migration's table set). Run by
-- PlatformFlywayRunner against schema "platform" specifically -- never mixed with the
-- tenant baseline in db/migration, and never applied to a tenant schema.

CREATE TABLE organization (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    slug VARCHAR(50) NOT NULL UNIQUE,
    schema_name VARCHAR(63) NOT NULL UNIQUE,
    is_default BOOLEAN NOT NULL DEFAULT false,
    logo_path VARCHAR(500),
    address VARCHAR(500),
    description VARCHAR(2000),
    status VARCHAR(20) NOT NULL,
    auth_mode VARCHAR(20) NOT NULL DEFAULT 'LOCAL',
    ldap_url VARCHAR(300),
    ldap_base VARCHAR(300),
    ldap_user_dn_pattern VARCHAR(300),
    ldap_user_search_filter VARCHAR(300),
    ldap_user_search_base VARCHAR(300),
    ldap_bind_dn VARCHAR(300),
    ldap_bind_password VARCHAR(300),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- At most one org may serve at the bare domain root (no /slug prefix).
CREATE UNIQUE INDEX uq_organization_is_default ON organization (is_default) WHERE is_default = true;

-- A Super Admin is not "part of" any organization, so it's a wholly separate identity
-- space from tenant-schema app_user rows -- own table, own login, own JWT type.
CREATE TABLE platform_admin (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(200) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
