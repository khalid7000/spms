package com.rit.spms.platform.migration;

import com.rit.spms.platform.domain.Organization;
import com.rit.spms.platform.domain.enums.OrgStatus;
import com.rit.spms.platform.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * Reaches every existing organization's schema with whatever new migrations have landed in
 * db/migration since it was provisioned -- a normal deploy with a new V84 etc. applies
 * everywhere automatically. Must be an {@link ApplicationRunner} rather than a plain
 * {@code @Bean} (compare {@link PlatformFlywayRunner}): it needs {@link OrganizationRepository},
 * a JPA repository, which isn't usable until the context is fully up. That's fine here --
 * unlike the platform schema migration, Hibernate's own bootstrap-time schema validation
 * only ever touches the default schema, never every tenant, so there's no ordering race to
 * protect against for this runner.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TenantMigrationRunner implements ApplicationRunner {

    private static final String TENANT_BASELINE_LOCATION = "classpath:db/migration";

    private final OrganizationRepository organizationRepository;
    private final DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) {
        List<Organization> activeOrgs = organizationRepository.findByStatus(OrgStatus.ACTIVE);
        for (Organization org : activeOrgs) {
            List<String> locations = new ArrayList<>();
            locations.add(TENANT_BASELINE_LOCATION);
            if (StringUtils.hasText(org.getExtraContentLocations())) {
                locations.addAll(List.of(org.getExtraContentLocations().split(",")));
            }

            log.info("Applying forward migrations to organization '{}' (schema '{}'), locations={}",
                    org.getSlug(), org.getSchemaName(), locations);
            Flyway.configure()
                    .dataSource(dataSource)
                    .schemas(org.getSchemaName())
                    .locations(locations.toArray(new String[0]))
                    .load()
                    .migrate();
        }
    }
}
