package com.rit.spms.dto.response;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * The central "My Tasks" dashboard payload (see the round-1 VSM plan's later phases): everything an
 * employee needs to answer "what's available for me to pull, and what am I already on the hook
 * for" in one call. Scoped to the viewer's own department for the available-to-pull side --
 * {@code null} department fields mean this employee has no department of their own (e.g. a pure
 * org-group head), so there's nothing to show there.
 */
@Value
@Builder
public class VsmDashboardResponse {
    Long departmentId;
    String departmentName;
    long availableToPullCount;
    List<ImprovementTaskResponse> availableToPullPreview;
    List<ImprovementTaskResponse> myTasks;
}
