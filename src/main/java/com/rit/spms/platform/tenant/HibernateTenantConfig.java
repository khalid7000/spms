package com.rit.spms.platform.tenant;

import org.hibernate.cfg.MultiTenancySettings;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Activates Hibernate's SCHEMA multi-tenancy strategy -- presence of these two properties
 * (pointing at our connection provider/identifier resolver beans) is what switches
 * Hibernate 6 into per-request schema routing; no separate enum property is needed. */
@Configuration
public class HibernateTenantConfig {

    @Bean
    public HibernatePropertiesCustomizer hibernateTenantPropertiesCustomizer(
            SchemaMultiTenantConnectionProvider connectionProvider,
            TenantIdentifierResolver identifierResolver) {
        return properties -> {
            properties.put(MultiTenancySettings.MULTI_TENANT_CONNECTION_PROVIDER, connectionProvider);
            properties.put(MultiTenancySettings.MULTI_TENANT_IDENTIFIER_RESOLVER, identifierResolver);
        };
    }
}
