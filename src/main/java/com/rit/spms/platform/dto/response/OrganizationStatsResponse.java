package com.rit.spms.platform.dto.response;

import com.rit.spms.platform.domain.enums.OrgStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/** One row of the Super Admin dashboard. Counts are 0 for any non-ACTIVE org (a
 * PROVISIONING/FAILED org's schema may not even have these tables yet) -- still listed,
 * with zero counts, so a stuck provisioning attempt is visible rather than hidden. */
@Data
@Builder
public class OrganizationStatsResponse {
    private Long id;
    private String name;
    private String slug;
    private Boolean isDefault;
    private String logoPath;
    private OrgStatus status;
    private LocalDateTime createdAt;
    private long userCount;
    private long strategyCount;
    private long notificationCount;
    private long initiativeCount;
}
