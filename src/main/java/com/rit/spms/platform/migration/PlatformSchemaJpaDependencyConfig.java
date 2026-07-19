package com.rit.spms.platform.migration;

import org.springframework.boot.autoconfigure.orm.jpa.EntityManagerFactoryDependsOnPostProcessor;
import org.springframework.context.annotation.Configuration;

/**
 * Forces JPA's EntityManagerFactory to wait for {@link PlatformFlywayRunner} to finish
 * before Hibernate validates the schema -- otherwise startup can race the platform
 * migration and fail against tables (organization, platform_admin) that don't exist yet.
 * Mirrors the same mechanism Spring Boot's own FlywayAutoConfiguration uses internally.
 */
@Configuration
public class PlatformSchemaJpaDependencyConfig extends EntityManagerFactoryDependsOnPostProcessor {
    public PlatformSchemaJpaDependencyConfig() {
        super("platformFlywayMigration");
    }
}
