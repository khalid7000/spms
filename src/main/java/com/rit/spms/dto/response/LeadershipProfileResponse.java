package com.rit.spms.dto.response;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/** Drives Strategy Creation Console visibility/options: what the current user heads, if anything. */
@Value
@Builder
public class LeadershipProfileResponse {
    List<DeptInfo> headedDepartments;
    List<OrgGroupInfo> headedOrgGroups;
    boolean hasDirectReports;
    /** True only when the user's org hierarchy spans more than the departments they head directly
     *  (e.g. a Dean/Provost over other heads) -- Organization Evaluations shows the exact same set
     *  of people as Team Evaluations otherwise, so it's not worth a separate console for them. */
    boolean hasMultiLevelHierarchy;

    public record DeptInfo(Long id, String name) {}
    public record OrgGroupInfo(Long id, String title, boolean isRoot) {}
}
