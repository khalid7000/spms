package com.rit.spms.controller;

import com.rit.spms.dto.request.AssignGoalAreaRequest;
import com.rit.spms.dto.request.CreateVisionAreaRequest;
import com.rit.spms.dto.response.ApiResponse;
import com.rit.spms.dto.response.VisionAreaResponse;
import com.rit.spms.security.UserPrincipal;
import com.rit.spms.service.VisionAreaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class VisionAreaController {

    private final VisionAreaService visionAreaService;

    @GetMapping("/api/strategies/{strategyId}/areas")
    public ResponseEntity<ApiResponse<List<VisionAreaResponse>>> getAreas(
            @PathVariable Long strategyId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                visionAreaService.getAreas(strategyId, principal.getId())));
    }

    @PostMapping("/api/strategies/{strategyId}/areas")
    public ResponseEntity<ApiResponse<VisionAreaResponse>> createArea(
            @PathVariable Long strategyId,
            @Valid @RequestBody CreateVisionAreaRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        VisionAreaResponse area = visionAreaService.createArea(strategyId, req, principal.getId());
        return ResponseEntity.status(201).body(ApiResponse.success("Vision area created", area));
    }

    @PutMapping("/api/areas/{id}")
    public ResponseEntity<ApiResponse<VisionAreaResponse>> updateArea(
            @PathVariable Long id,
            @Valid @RequestBody CreateVisionAreaRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                visionAreaService.updateArea(id, req, principal.getId())));
    }

    @DeleteMapping("/api/areas/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteArea(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        visionAreaService.deleteArea(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Vision area deleted", null));
    }

    /** Owner-only: assign or remove a goal from a concentration area. areaId null = ungrouped. */
    @PatchMapping("/api/goals/{goalId}/area")
    public ResponseEntity<ApiResponse<VisionAreaResponse>> assignGoalArea(
            @PathVariable Long goalId,
            @RequestBody AssignGoalAreaRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        VisionAreaResponse area = visionAreaService.assignGoalToArea(goalId, req.getAreaId(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Goal area updated", area));
    }
}
