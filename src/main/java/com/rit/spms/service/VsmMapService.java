package com.rit.spms.service;

import com.rit.spms.domain.AppUser;
import com.rit.spms.domain.Department;
import com.rit.spms.domain.OrgGroup;
import com.rit.spms.domain.OrganizationSetting;
import com.rit.spms.domain.VsmEdge;
import com.rit.spms.domain.VsmMap;
import com.rit.spms.domain.VsmNode;
import com.rit.spms.domain.VsmNodeMetric;
import com.rit.spms.domain.enums.VsmEdgeType;
import com.rit.spms.domain.enums.VsmNodeType;
import com.rit.spms.domain.enums.VsmNotationPack;
import com.rit.spms.domain.enums.VsmScopeType;
import com.rit.spms.domain.enums.SystemRole;
import com.rit.spms.dto.request.CreateVsmMapRequest;
import com.rit.spms.dto.request.UpdateVsmMapRequest;
import com.rit.spms.dto.request.VsmCanvasSaveRequest;
import com.rit.spms.dto.response.VsmEdgeResponse;
import com.rit.spms.dto.response.VsmMapResponse;
import com.rit.spms.dto.response.VsmMapSummaryResponse;
import com.rit.spms.dto.response.VsmNodeResponse;
import com.rit.spms.exception.BusinessRuleException;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.repository.AppUserRepository;
import com.rit.spms.repository.DepartmentRepository;
import com.rit.spms.repository.OrgGroupRepository;
import com.rit.spms.repository.OrganizationSettingRepository;
import com.rit.spms.repository.VsmEdgeRepository;
import com.rit.spms.repository.VsmMapRepository;
import com.rit.spms.repository.VsmNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CRUD for VsmMap/VsmNode/VsmEdge (Phase 1 of the Value Stream Mapping module -- see the round-1
 * design plan), plus {@link #applyGeneratedDraft} which VsmDraftGenerationService's async job (Phase
 * 2) calls once an AI draft is ready. No Kaizen-burst/Kanban task wiring yet; that's Phase 3, layered
 * on top of this without changing what's here.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class VsmMapService {

    private static final String NOTATION_PACKS_SETTING_KEY = "VSM_ENABLED_NOTATION_PACKS";

    private final VsmMapRepository vsmMapRepository;
    private final VsmNodeRepository vsmNodeRepository;
    private final VsmEdgeRepository vsmEdgeRepository;
    private final AppUserRepository appUserRepository;
    private final DepartmentRepository departmentRepository;
    private final OrgGroupRepository orgGroupRepository;
    private final OrganizationSettingRepository organizationSettingRepository;
    private final PermissionService permissionService;

    /** Maps visible to this user: everything they authored, plus everything owned by a department or
     *  org group they head. Admins see every map. */
    @Transactional(readOnly = true)
    public List<VsmMapSummaryResponse> listMyMaps(Long userId) {
        return resolveVisibleMaps(userId).stream().map(VsmMapSummaryResponse::from).toList();
    }

    /** Same visibility rule {@link #listMyMaps} exposes as summaries, as entities -- shared with
     *  {@link VsmAnalyticsService}'s cross-map rollup (Phase 6) so the analytics dashboard covers
     *  exactly the same set of maps a user already sees in their own list, nothing more or less. */
    @Transactional(readOnly = true)
    public List<VsmMap> resolveVisibleMaps(Long userId) {
        AppUser user = requireUser(userId);
        if (user.hasRole(SystemRole.ADMIN)) {
            return vsmMapRepository.findAll();
        }
        Set<Long> departmentIds = departmentRepository.findByHeadId(userId).stream()
                .map(Department::getId).collect(Collectors.toSet());
        Set<Long> orgGroupIds = orgGroupRepository.findByHeadId(userId).stream()
                .map(OrgGroup::getId).collect(Collectors.toSet());
        Map<Long, VsmMap> byId = new HashMap<>();
        if (!departmentIds.isEmpty()) {
            vsmMapRepository.findByDepartmentIdIn(departmentIds).forEach(m -> byId.put(m.getId(), m));
        }
        if (!orgGroupIds.isEmpty()) {
            vsmMapRepository.findByOrgGroupIdIn(orgGroupIds).forEach(m -> byId.put(m.getId(), m));
        }
        if (user.getDepartment() != null) {
            vsmMapRepository.findByDepartmentId(user.getDepartment().getId()).forEach(m -> byId.put(m.getId(), m));
        }
        return new ArrayList<>(byId.values());
    }

    /** Every VsmNodeType this installation's Admin has enabled via the VSM_ENABLED_NOTATION_PACKS
     *  organization_setting (see VsmNotationPack) -- what the canvas palette should offer. */
    @Transactional(readOnly = true)
    public List<VsmNodeType> getAvailableNodeTypes() {
        Set<VsmNotationPack> enabledPacks = getEnabledNotationPacks();
        return Arrays.stream(VsmNodeType.values())
                .filter(t -> enabledPacks.contains(t.getPack()))
                .toList();
    }

    private Set<VsmNotationPack> getEnabledNotationPacks() {
        String value = organizationSettingRepository.findBySettingKey(NOTATION_PACKS_SETTING_KEY)
                .map(OrganizationSetting::getValue)
                .orElse(VsmNotationPack.GENERIC.name());
        Set<VsmNotationPack> packs = new LinkedHashSet<>();
        for (String code : value.split(",")) {
            String trimmed = code.trim();
            if (!trimmed.isEmpty()) {
                packs.add(VsmNotationPack.valueOf(trimmed));
            }
        }
        packs.add(VsmNotationPack.GENERIC); // always available regardless of setting
        return packs;
    }

    public VsmMap createMap(CreateVsmMapRequest req, Long userId) {
        permissionService.assertCanCreateVsmMap(userId, req.getScopeType(), req.getScopeId());
        AppUser creator = requireUser(userId);

        VsmMap.VsmMapBuilder builder = VsmMap.builder()
                .scopeType(req.getScopeType())
                .title(req.getTitle())
                .description(req.getDescription())
                .createdBy(creator);

        if (req.getScopeType() == VsmScopeType.DEPARTMENT) {
            Department dept = departmentRepository.findById(req.getScopeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", req.getScopeId()));
            builder.department(dept);
        } else {
            OrgGroup group = orgGroupRepository.findById(req.getScopeId())
                    .orElseThrow(() -> new ResourceNotFoundException("OrgGroup", req.getScopeId()));
            builder.orgGroup(group);
        }

        return vsmMapRepository.save(builder.build());
    }

    @Transactional(readOnly = true)
    public VsmMapResponse getMapDetail(Long mapId, Long userId) {
        VsmMap map = requireMap(mapId);
        permissionService.assertCanViewVsmMap(userId, map);
        return buildMapResponse(map, null, userId);
    }

    public VsmMap updateMap(Long mapId, UpdateVsmMapRequest req, Long userId) {
        VsmMap map = requireMap(mapId);
        permissionService.assertCanEditVsmMap(userId, map);
        map.setTitle(req.getTitle());
        map.setDescription(req.getDescription());
        if (req.getState() != null) {
            map.setState(req.getState());
        }
        return vsmMapRepository.save(map);
    }

    public void deleteMap(Long mapId, Long userId) {
        VsmMap map = requireMap(mapId);
        permissionService.assertCanEditVsmMap(userId, map);
        vsmMapRepository.delete(map); // cascades to vsm_node/vsm_edge/vsm_node_metric at the DB level
    }

    /**
     * Replaces the map's entire node/edge/metric set in one call. New nodes (id == null) are saved
     * first so they get real ids, then edges are resolved against a ref->id table keyed by both
     * every node's real id (as a string) and, for new nodes, their client-supplied tempId -- letting
     * an edge in the same payload reference a node that didn't exist before this call. Any existing
     * node not present in the payload is deleted; existing edges are always fully replaced.
     */
    public VsmMapResponse saveCanvas(Long mapId, VsmCanvasSaveRequest req, Long userId) {
        VsmMap map = requireMap(mapId);
        permissionService.assertCanEditVsmMap(userId, map);

        // Palette gating (which VsmNodeTypes an Admin has enabled) is enforced here too, not just by
        // the frontend hiding the button -- same "defensive re-filter" reasoning already applied to
        // the AI draft path in VsmDraftGenerationService.
        Set<VsmNodeType> enabledNodeTypes = new HashSet<>(getAvailableNodeTypes());
        for (VsmCanvasSaveRequest.NodeItem item : req.getNodes()) {
            if (!enabledNodeTypes.contains(item.getNodeType())) {
                throw new BusinessRuleException(
                        "Node type " + item.getNodeType() + " is not enabled for this installation");
            }
        }

        Map<Long, VsmNode> existingNodesById = vsmNodeRepository.findByVsmMapIdOrderById(mapId).stream()
                .collect(Collectors.toMap(VsmNode::getId, n -> n));

        // Wipe edges up front -- they're always fully replaced, and this avoids FK conflicts while
        // nodes below are being deleted/recreated.
        vsmEdgeRepository.deleteByVsmMapId(mapId);

        Set<Long> keptNodeIds = new HashSet<>();
        Map<String, Long> refToRealId = new HashMap<>();
        Map<String, Long> tempIdMapping = new HashMap<>();

        for (VsmCanvasSaveRequest.NodeItem item : req.getNodes()) {
            VsmNode node;
            if (item.getId() != null) {
                node = existingNodesById.get(item.getId());
                if (node == null) {
                    throw new ResourceNotFoundException("VsmNode on this map", item.getId());
                }
                keptNodeIds.add(item.getId());
            } else {
                node = new VsmNode();
                node.setVsmMap(map);
            }
            node.setNodeType(item.getNodeType());
            node.setPositionX(item.getPositionX());
            node.setPositionY(item.getPositionY());
            node.setTitle(item.getTitle());
            node.setDescription(item.getDescription());
            node.setCycleTimeMinutes(item.getCycleTimeMinutes());
            node.setCompleteAccuratePercent(item.getCompleteAccuratePercent());
            node.setFailRatePercent(item.getFailRatePercent());

            node.getMetrics().clear();
            if (item.getMetrics() != null) {
                int order = 0;
                for (VsmCanvasSaveRequest.MetricItem m : item.getMetrics()) {
                    VsmNodeMetric metric = VsmNodeMetric.builder()
                            .node(node)
                            .label(m.getLabel())
                            .value(m.getValue())
                            .unit(m.getUnit())
                            .displayOrder(m.getDisplayOrder() != null ? m.getDisplayOrder() : order)
                            .build();
                    node.getMetrics().add(metric);
                    order++;
                }
            }

            node = vsmNodeRepository.save(node);
            refToRealId.put(String.valueOf(node.getId()), node.getId());
            if (item.getId() == null && item.getTempId() != null) {
                refToRealId.put(item.getTempId(), node.getId());
                tempIdMapping.put(item.getTempId(), node.getId());
            }
        }

        for (Map.Entry<Long, VsmNode> entry : existingNodesById.entrySet()) {
            if (!keptNodeIds.contains(entry.getKey())) {
                vsmNodeRepository.delete(entry.getValue());
            }
        }

        List<VsmEdge> edges = new ArrayList<>();
        for (VsmCanvasSaveRequest.EdgeItem item : req.getEdges()) {
            Long sourceId = refToRealId.get(item.getSourceRef());
            Long targetId = refToRealId.get(item.getTargetRef());
            if (sourceId == null || targetId == null) {
                throw new BusinessRuleException(
                        "Edge references a node that is not part of this save: " + item.getSourceRef() + " -> " + item.getTargetRef());
            }
            VsmNode source = vsmNodeRepository.findById(sourceId)
                    .orElseThrow(() -> new ResourceNotFoundException("VsmNode", sourceId));
            VsmNode target = vsmNodeRepository.findById(targetId)
                    .orElseThrow(() -> new ResourceNotFoundException("VsmNode", targetId));
            edges.add(VsmEdge.builder()
                    .vsmMap(map)
                    .sourceNode(source)
                    .targetNode(target)
                    .edgeType(item.getEdgeType() != null ? item.getEdgeType() : VsmEdgeType.MATERIAL_FLOW)
                    .label(item.getLabel())
                    .build());
        }
        vsmEdgeRepository.saveAll(edges);

        return buildMapResponse(map, tempIdMapping, userId);
    }

    private VsmMapResponse buildMapResponse(VsmMap map, Map<String, Long> tempIdMapping, Long userId) {
        List<VsmNodeResponse> nodes = vsmNodeRepository.findByVsmMapIdOrderById(map.getId()).stream()
                .map(VsmNodeResponse::from).toList();
        List<VsmEdgeResponse> edges = vsmEdgeRepository.findByVsmMapId(map.getId()).stream()
                .map(VsmEdgeResponse::from).toList();
        return VsmMapResponse.builder()
                .id(map.getId())
                .scopeType(map.getScopeType())
                .departmentId(map.getDepartment() != null ? map.getDepartment().getId() : null)
                .departmentName(map.getDepartment() != null ? map.getDepartment().getName() : null)
                .orgGroupId(map.getOrgGroup() != null ? map.getOrgGroup().getId() : null)
                .orgGroupName(map.getOrgGroup() != null ? map.getOrgGroup().getTitle() : null)
                .title(map.getTitle())
                .description(map.getDescription())
                .state(map.getState())
                .canEdit(permissionService.canEditVsmMap(userId, map))
                .updatedAt(map.getUpdatedAt())
                .nodes(nodes)
                .edges(edges)
                .tempIdMapping(tempIdMapping)
                .generationRequestedAt(map.getGenerationRequestedAt())
                .generatedAt(map.getGeneratedAt())
                .generationFailureReason(map.getGenerationFailureReason())
                .draftProcessDescription(map.getDraftProcessDescription())
                .build();
    }

    /**
     * Persists an AI-generated draft's nodes/edges as real rows on the map -- called by
     * VsmDraftGenerationService's async job once generation succeeds. Assumes the map is empty
     * (true for the "describe your process" creation path this backs); a simple two-row default
     * layout (kaizen bursts on their own row) gives the leader something reasonable to look at
     * before they drag anything around.
     */
    public void applyGeneratedDraft(VsmMap map, List<VsmDraftGenerator.SuggestedNodeDto> draftNodes,
                                     List<VsmDraftGenerator.SuggestedEdgeDto> draftEdges) {
        Map<String, Long> tempIdToRealId = new HashMap<>();
        int rowIndex = 0;
        int burstIndex = 0;
        for (VsmDraftGenerator.SuggestedNodeDto n : draftNodes) {
            boolean isBurst = n.nodeType() == VsmNodeType.KAIZEN_BURST;
            VsmNode node = VsmNode.builder()
                    .vsmMap(map)
                    .nodeType(n.nodeType())
                    .title(n.title())
                    .description(n.description())
                    .cycleTimeMinutes(n.cycleTimeMinutes())
                    .completeAccuratePercent(n.completeAccuratePercent())
                    .failRatePercent(n.failRatePercent())
                    .positionX(80.0 + (isBurst ? burstIndex : rowIndex) * 240.0)
                    .positionY(isBurst ? 320.0 : 120.0)
                    .build();
            node = vsmNodeRepository.save(node);
            tempIdToRealId.put(n.tempId(), node.getId());
            if (isBurst) burstIndex++; else rowIndex++;
        }
        for (VsmDraftGenerator.SuggestedEdgeDto e : draftEdges) {
            Long sourceId = tempIdToRealId.get(e.sourceTempId());
            Long targetId = tempIdToRealId.get(e.targetTempId());
            if (sourceId == null || targetId == null) {
                continue; // VsmDraftGenerationService already filters dangling refs -- never trust twice
            }
            vsmEdgeRepository.save(VsmEdge.builder()
                    .vsmMap(map)
                    .sourceNode(vsmNodeRepository.getReferenceById(sourceId))
                    .targetNode(vsmNodeRepository.getReferenceById(targetId))
                    .edgeType(e.edgeType() != null ? e.edgeType() : VsmEdgeType.MATERIAL_FLOW)
                    .label(e.label())
                    .build());
        }
    }

    private VsmMap requireMap(Long mapId) {
        return vsmMapRepository.findById(mapId)
                .orElseThrow(() -> new ResourceNotFoundException("VsmMap", mapId));
    }

    private AppUser requireUser(Long userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", userId));
    }
}
