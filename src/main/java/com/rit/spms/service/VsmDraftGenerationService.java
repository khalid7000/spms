package com.rit.spms.service;

import com.rit.spms.domain.VsmMap;
import com.rit.spms.domain.enums.VsmNodeType;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.repository.VsmMapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Orchestrates {@link VsmDraftGenerator} for the "describe your process, let AI draft it" map
 * creation path (Phase 2) -- ASYNC background job, same pattern as
 * EmployeeGoalSuggestionService/TeachingEvaluationSessionService: {@link #recordGenerationRequested}
 * commits in its own transaction (so the timestamp is visible before the background thread starts),
 * then {@link #generateDraftAsync} runs on a separate thread (Spring {@code @Async}, backed by
 * {@code @EnableAsync} on {@code SpmsApplication}) in its own {@code REQUIRES_NEW} transaction. Both
 * must be called directly from the controller, never from inside another {@code @Transactional}
 * service method, or the "requested" write won't have committed yet when the async thread reads it.
 * "Generating"/"failed"/"done" has no dedicated status enum -- the frontend derives it from
 * generationRequestedAt/generatedAt/generationFailureReason, polling VsmMapController#getMap.
 */
@Service
@RequiredArgsConstructor
public class VsmDraftGenerationService {

    private final VsmDraftGenerator vsmDraftGenerator;
    private final VsmMapService vsmMapService;
    private final VsmMapRepository vsmMapRepository;
    private final PermissionService permissionService;

    /** Records the request and the input text so the async job (and a retry after failure, or a
     *  page reload mid-generation) can read everything it needs from the map row alone. Must commit
     *  before {@link #generateDraftAsync} is invoked. */
    @Transactional
    public VsmMap recordGenerationRequested(Long mapId, String processDescription, Long userId) {
        VsmMap map = requireMap(mapId);
        permissionService.assertCanEditVsmMap(userId, map);
        map.setDraftProcessDescription(processDescription);
        map.setGenerationRequestedAt(LocalDateTime.now());
        map.setGenerationFailureReason(null);
        return vsmMapRepository.save(map);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void generateDraftAsync(Long mapId) {
        VsmMap map = requireMap(mapId);
        try {
            List<VsmNodeType> allowedNodeTypes = vsmMapService.getAvailableNodeTypes();
            VsmDraftGenerator.SuggestedMapDto draft =
                    vsmDraftGenerator.generateDraft(map.getDraftProcessDescription(), allowedNodeTypes);

            // Defensive re-filter: the prompt already restricts the model to allowedNodeTypes, but a
            // model can still hallucinate outside it -- never let a disabled-pack symbol (or an edge
            // pointing at a node we just dropped) leak into what gets persisted.
            Set<VsmNodeType> allowedSet = new HashSet<>(allowedNodeTypes);
            List<VsmDraftGenerator.SuggestedNodeDto> keptNodes = draft.nodes().stream()
                    .filter(n -> allowedSet.contains(n.nodeType()))
                    .toList();
            Set<String> keptTempIds = new HashSet<>();
            keptNodes.forEach(n -> keptTempIds.add(n.tempId()));
            List<VsmDraftGenerator.SuggestedEdgeDto> keptEdges = draft.edges().stream()
                    .filter(e -> keptTempIds.contains(e.sourceTempId()) && keptTempIds.contains(e.targetTempId()))
                    .toList();

            vsmMapService.applyGeneratedDraft(map, keptNodes, keptEdges);
            map.setGeneratedAt(LocalDateTime.now());
            map.setGenerationFailureReason(null);
            vsmMapRepository.save(map);
        } catch (Exception e) {
            map.setGenerationFailureReason(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            vsmMapRepository.save(map);
        }
    }

    private VsmMap requireMap(Long mapId) {
        return vsmMapRepository.findById(mapId)
                .orElseThrow(() -> new ResourceNotFoundException("VsmMap", mapId));
    }
}
