package com.rit.spms.service;

import com.rit.spms.domain.ImprovementTask;
import com.rit.spms.domain.VsmMap;
import com.rit.spms.domain.VsmNode;
import com.rit.spms.domain.enums.VsmMapState;
import com.rit.spms.domain.enums.VsmNodeType;
import com.rit.spms.domain.enums.VsmTaskState;
import com.rit.spms.domain.enums.VsmTaskType;
import com.rit.spms.dto.response.VsmAnalyticsResponse;
import com.rit.spms.repository.ImprovementTaskRepository;
import com.rit.spms.repository.VsmNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Phase 6 of the VSM module: a cross-map rollup dashboard over every {@link VsmMap} a user can see
 * (same visibility as {@link VsmMapService#resolveVisibleMaps}). See {@link VsmAnalyticsResponse}'s
 * class doc for why this is a current-state snapshot rather than a trend-over-time chart.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VsmAnalyticsService {

    private static final int HOTSPOT_LIMIT = 10;

    private final VsmMapService vsmMapService;
    private final VsmNodeRepository vsmNodeRepository;
    private final ImprovementTaskRepository improvementTaskRepository;

    public VsmAnalyticsResponse getRollup(Long userId) {
        List<VsmMap> maps = vsmMapService.resolveVisibleMaps(userId);
        Set<Long> mapIds = maps.stream().map(VsmMap::getId).collect(Collectors.toSet());
        Map<Long, String> mapTitleById = maps.stream()
                .collect(Collectors.toMap(VsmMap::getId, VsmMap::getTitle));

        Map<VsmMapState, Long> mapsByState = new EnumMap<>(VsmMapState.class);
        for (VsmMapState state : VsmMapState.values()) {
            mapsByState.put(state, maps.stream().filter(m -> m.getState() == state).count());
        }

        List<VsmNode> nodes = mapIds.isEmpty() ? List.of() : vsmNodeRepository.findByVsmMapIdIn(mapIds);
        List<VsmAnalyticsResponse.NodeHotspot> topFailRateNodes = topProcessNodesBy(
                nodes, mapTitleById, VsmNode::getFailRatePercent);
        List<VsmAnalyticsResponse.NodeHotspot> topCycleTimeNodes = topProcessNodesBy(
                nodes, mapTitleById, VsmNode::getCycleTimeMinutes);

        List<ImprovementTask> tasks = mapIds.isEmpty()
                ? List.of() : improvementTaskRepository.findByKaizenNode_VsmMap_IdIn(mapIds);

        Map<VsmTaskState, Long> tasksByState = new EnumMap<>(VsmTaskState.class);
        for (VsmTaskState state : VsmTaskState.values()) {
            tasksByState.put(state, tasks.stream().filter(t -> t.getState() == state).count());
        }
        Map<VsmTaskType, Long> tasksByType = new EnumMap<>(VsmTaskType.class);
        for (VsmTaskType type : VsmTaskType.values()) {
            tasksByType.put(type, tasks.stream().filter(t -> t.getTaskType() == type).count());
        }

        long improvementTotal = tasks.stream().filter(t -> t.getTaskType() == VsmTaskType.IMPROVEMENT).count();
        long improvementWithAchievement = tasks.stream()
                .filter(t -> t.getTaskType() == VsmTaskType.IMPROVEMENT && t.getAchievement() != null)
                .count();

        return VsmAnalyticsResponse.builder()
                .totalMaps(maps.size())
                .mapsByState(mapsByState)
                .totalTasks(tasks.size())
                .tasksByState(tasksByState)
                .tasksByType(tasksByType)
                .improvementTasksTotal(improvementTotal)
                .improvementTasksWithAchievement(improvementWithAchievement)
                .topFailRateNodes(topFailRateNodes)
                .topCycleTimeNodes(topCycleTimeNodes)
                .build();
    }

    private List<VsmAnalyticsResponse.NodeHotspot> topProcessNodesBy(
            List<VsmNode> nodes, Map<Long, String> mapTitleById,
            java.util.function.Function<VsmNode, BigDecimal> valueOf) {
        return nodes.stream()
                .filter(n -> n.getNodeType() == VsmNodeType.PROCESS && valueOf.apply(n) != null)
                .sorted(Comparator.comparing(valueOf).reversed())
                .limit(HOTSPOT_LIMIT)
                .map(n -> new VsmAnalyticsResponse.NodeHotspot(
                        n.getVsmMap().getId(), mapTitleById.get(n.getVsmMap().getId()),
                        n.getId(), n.getTitle(), valueOf.apply(n)))
                .toList();
    }
}
