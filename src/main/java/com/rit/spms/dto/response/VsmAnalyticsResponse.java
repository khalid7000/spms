package com.rit.spms.dto.response;

import com.rit.spms.domain.enums.VsmMapState;
import com.rit.spms.domain.enums.VsmTaskState;
import com.rit.spms.domain.enums.VsmTaskType;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Cross-map rollup (Phase 6 of the VSM module) over every map a user can see -- the same set
 * {@link com.rit.spms.service.VsmMapService#resolveVisibleMaps} already resolves for their own map
 * list. Deliberately a current-state snapshot, not a trend over time: {@code VsmNodeMetric}/the
 * typed metric columns on {@code VsmNode} hold live values, not a time series, so a "fail rate over
 * the last 6 months" chart isn't buildable without first deciding a snapshot cadence or adding an
 * effectiveDate column -- a decision the round-1 plan deferred until real usage data exists to
 * design it against. This rollup answers "what does it look like right now," which the data already
 * supports well.
 */
@Value
@Builder
public class VsmAnalyticsResponse {
    long totalMaps;
    Map<VsmMapState, Long> mapsByState;

    long totalTasks;
    Map<VsmTaskState, Long> tasksByState;
    Map<VsmTaskType, Long> tasksByType;

    /** Of all IMPROVEMENT tasks (any state), how many have already been traced to a real
     *  Achievement -- a quick read on how much of the module's activity has actually landed in
     *  someone's portfolio versus is still in flight. */
    long improvementTasksTotal;
    long improvementTasksWithAchievement;

    /** Top 10 PROCESS nodes by fail rate / cycle time across every visible map, worst first --
     *  the bottleneck leaderboard this whole module exists to surface. Empty if no PROCESS node
     *  anywhere has that metric filled in yet. */
    List<NodeHotspot> topFailRateNodes;
    List<NodeHotspot> topCycleTimeNodes;

    public record NodeHotspot(
            Long mapId, String mapTitle, Long nodeId, String nodeTitle, BigDecimal value) {
    }
}
