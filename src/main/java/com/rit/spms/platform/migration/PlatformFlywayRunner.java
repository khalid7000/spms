package com.rit.spms.platform.migration;

import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Runs the platform tier's own small migration set (organization/platform_admin tables)
 * against a fixed "platform" schema -- entirely separate from the tenant baseline in
 * db/migration, which keeps running unchanged via Spring Boot's autoconfigured Flyway
 * against the default schema until the Hibernate multi-tenancy wiring (next phase) takes
 * over schema selection per request.
 *
 * <p>Registered as a plain {@code @Bean} rather than an {@code ApplicationRunner}: an
 * ApplicationRunner fires after the context (and therefore Hibernate's schema validation)
 * has already started, which would race this migration. Being a {@code @Bean} lets
 * {@link PlatformSchemaJpaDependencyConfig} force the JPA EntityManagerFactory to depend on
 * it, guaranteeing this runs first.
 *
 * <p>The bean's return type is deliberately {@link Boolean}, not {@link Flyway} -- Spring
 * Boot's own {@code FlywayAutoConfiguration} only creates its default {@code Flyway} bean
 * (the one that runs db/migration against the default schema) when no {@code Flyway}-typed
 * bean already exists ({@code @ConditionalOnMissingBean(Flyway.class)}). Returning a
 * {@code Flyway} here would silently back that off and stop the tenant baseline migration
 * from running at all.
 */
@Configuration
public class PlatformFlywayRunner {

    public static final String PLATFORM_SCHEMA = "platform";

    @Bean
    public Boolean platformFlywayMigration(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas(PLATFORM_SCHEMA)
                .createSchemas(true)
                .locations("classpath:db/migration-platform")
                .load();
        flyway.migrate();
        return true;
    }
}
