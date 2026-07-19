package com.rit.spms.platform.service;

import com.rit.spms.platform.domain.Organization;
import com.rit.spms.platform.domain.enums.OrgStatus;
import com.rit.spms.platform.dto.response.OrganizationStatsResponse;
import com.rit.spms.platform.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Powers the Super Admin dashboard: every registered organization, with per-schema counts
 * of users/strategies/notifications/initiatives.
 *
 * <p>Counts are computed via one {@code UNION ALL} query per metric across every ACTIVE
 * org's schema -- 4 queries total regardless of org count, not 4xN. Schema names spliced
 * into the SQL are always ones already stored in the registry at provisioning time (never
 * taken from a request), matching the same safety rule {@code OrganizationProvisioningService}
 * already follows for {@code CREATE SCHEMA}. Non-ACTIVE orgs (PROVISIONING/FAILED) are
 * skipped for counting -- their schema may not even have these tables yet -- and simply
 * reported with zero counts, so a stuck provisioning attempt stays visible instead of
 * silently vanishing from the list.
 */
@Service
@RequiredArgsConstructor
public class PlatformDashboardService {

    private static final List<String> COUNTED_TABLES =
            List.of("app_user", "strategy", "notification", "initiative");

    private final OrganizationRepository organizationRepository;
    private final JdbcTemplate jdbcTemplate;

    public List<OrganizationStatsResponse> getDashboard() {
        List<Organization> orgs = organizationRepository.findAll();
        List<String> activeSchemas = orgs.stream()
                .filter(o -> o.getStatus() == OrgStatus.ACTIVE)
                .map(Organization::getSchemaName)
                .toList();

        Map<String, Map<String, Long>> countsBySchema = new HashMap<>();
        for (String table : COUNTED_TABLES) {
            for (Map.Entry<String, Long> e : countAcrossSchemas(activeSchemas, table).entrySet()) {
                countsBySchema.computeIfAbsent(e.getKey(), k -> new HashMap<>()).put(table, e.getValue());
            }
        }

        return orgs.stream().map(o -> {
            Map<String, Long> counts = countsBySchema.getOrDefault(o.getSchemaName(), Map.of());
            return OrganizationStatsResponse.builder()
                    .id(o.getId())
                    .name(o.getName())
                    .slug(o.getSlug())
                    .isDefault(o.getIsDefault())
                    .logoPath(o.getLogoPath())
                    .status(o.getStatus())
                    .createdAt(o.getCreatedAt())
                    .userCount(counts.getOrDefault("app_user", 0L))
                    .strategyCount(counts.getOrDefault("strategy", 0L))
                    .notificationCount(counts.getOrDefault("notification", 0L))
                    .initiativeCount(counts.getOrDefault("initiative", 0L))
                    .build();
        }).toList();
    }

    private Map<String, Long> countAcrossSchemas(List<String> schemas, String table) {
        if (schemas.isEmpty()) {
            return Map.of();
        }
        String sql = schemas.stream()
                .map(s -> "SELECT '" + s + "' AS schema_name, count(*) AS cnt FROM \"" + s + "\"." + table)
                .collect(Collectors.joining(" UNION ALL "));
        return jdbcTemplate.query(sql, rs -> {
            Map<String, Long> result = new HashMap<>();
            while (rs.next()) {
                result.put(rs.getString("schema_name"), rs.getLong("cnt"));
            }
            return result;
        });
    }
}
