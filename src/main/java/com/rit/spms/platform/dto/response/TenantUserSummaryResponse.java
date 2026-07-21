package com.rit.spms.platform.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/** One AppUser row from a tenant org's own schema, as seen by the Super Admin -- lets them
 * confirm who the org's seeded/created users are without needing tenant-level access. */
@Data
@Builder
public class TenantUserSummaryResponse {
    private Long id;
    private String fname;
    private String lname;
    private String email;
    private boolean active;
    private boolean mustChangePassword;
    private List<String> roles;
}
