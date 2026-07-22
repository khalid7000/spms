package com.rit.spms.controller;

import com.rit.spms.domain.VsmMap;
import com.rit.spms.domain.enums.VsmNodeType;
import com.rit.spms.dto.request.CreateVsmMapRequest;
import com.rit.spms.dto.request.UpdateVsmMapRequest;
import com.rit.spms.dto.request.VsmCanvasSaveRequest;
import com.rit.spms.dto.request.VsmDraftRequest;
import com.rit.spms.dto.response.ApiResponse;
import com.rit.spms.dto.response.VsmAnalyticsResponse;
import com.rit.spms.dto.response.VsmMapResponse;
import com.rit.spms.dto.response.VsmMapSummaryResponse;
import com.rit.spms.security.UserPrincipal;
import com.rit.spms.service.VsmAnalyticsService;
import com.rit.spms.service.VsmDraftGenerationService;
import com.rit.spms.service.VsmMapService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Value Stream Map CRUD + canvas save (Phase 1) + async AI draft generation kickoff/retry (Phase 2,
 *  see VsmDraftGenerationService). Kaizen-burst/Kanban task endpoints are a later phase, added under
 *  this same /api/vsm prefix without touching this class. */
@RestController
@RequestMapping("/api/vsm/maps")
@RequiredArgsConstructor
public class VsmMapController {

    private final VsmMapService vsmMapService;
    private final VsmDraftGenerationService vsmDraftGenerationService;
    private final VsmAnalyticsService vsmAnalyticsService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<VsmMapSummaryResponse>>> listMyMaps(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(vsmMapService.listMyMaps(principal.getId())));
    }

    /** Cross-map rollup dashboard (Phase 6) over every map this user can see -- same visibility as
     *  {@link #listMyMaps}, aggregated. */
    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<VsmAnalyticsResponse>> getAnalytics(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(vsmAnalyticsService.getRollup(principal.getId())));
    }

    /** Which VsmNodeType symbols this installation's Admin has enabled -- drives the canvas palette. */
    @GetMapping("/available-node-types")
    public ResponseEntity<ApiResponse<List<VsmNodeType>>> getAvailableNodeTypes() {
        return ResponseEntity.ok(ApiResponse.success(vsmMapService.getAvailableNodeTypes()));
    }

    /**
     * Kicks off (or retries, after a failure) async AI draft generation for this map -- returns
     * immediately (202) with the map's current state, now showing generationRequestedAt set; the
     * frontend polls GET /{id} for generatedAt/generationFailureReason. Both calls below must happen
     * directly here, not nested inside another @Transactional method -- see
     * VsmDraftGenerationService's class javadoc for why.
     */
    @PostMapping("/{id}/generate-draft")
    public ResponseEntity<ApiResponse<VsmMapResponse>> generateDraft(
            @PathVariable Long id,
            @Valid @RequestBody VsmDraftRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        vsmDraftGenerationService.recordGenerationRequested(id, req.getProcessDescription(), principal.getId());
        vsmDraftGenerationService.generateDraftAsync(id);
        return ResponseEntity.status(202).body(ApiResponse.success(
                vsmMapService.getMapDetail(id, principal.getId())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VsmMapResponse>> createMap(
            @Valid @RequestBody CreateVsmMapRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        VsmMap map = vsmMapService.createMap(req, principal.getId());
        return ResponseEntity.status(201).body(ApiResponse.success("Value Stream Map created",
                vsmMapService.getMapDetail(map.getId(), principal.getId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VsmMapResponse>> getMap(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(vsmMapService.getMapDetail(id, principal.getId())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VsmMapResponse>> updateMap(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVsmMapRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        vsmMapService.updateMap(id, req, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Value Stream Map updated",
                vsmMapService.getMapDetail(id, principal.getId())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMap(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        vsmMapService.deleteMap(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Value Stream Map deleted", null));
    }

    @PutMapping("/{id}/canvas")
    public ResponseEntity<ApiResponse<VsmMapResponse>> saveCanvas(
            @PathVariable Long id,
            @Valid @RequestBody VsmCanvasSaveRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Canvas saved",
                vsmMapService.saveCanvas(id, req, principal.getId())));
    }
}
