package com.rit.spms.platform.dto.response;

import com.rit.spms.platform.domain.enums.AuthMode;
import com.rit.spms.platform.domain.enums.OrgStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/** Deliberately excludes {@code Organization}'s LDAP fields (bind DN/password etc.) -- those
 * are internal config, never meant to round-trip through an API response. */
@Data
@Builder
public class OrganizationResponse {
    private Long id;
    private String name;
    private String slug;
    private Boolean isDefault;
    private String logoPath;
    private String address;
    private String description;
    private OrgStatus status;
    private AuthMode authMode;
    private LocalDateTime createdAt;
}
