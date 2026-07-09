package com.rit.spms.controller;

import com.rit.spms.dto.response.ApiResponse;
import com.rit.spms.dto.response.CoverageReportResponse;
import com.rit.spms.dto.response.InitiativeResponse;
import com.rit.spms.dto.response.ObjectiveResponse;
import com.rit.spms.security.UserPrincipal;
import com.rit.spms.service.MappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MappingController {

    private final MappingService mappingService;

    @GetMapping("/strategies/{strategyId}/coverage")
    public ResponseEntity<ApiResponse<CoverageReportResponse>> getCoverage(
            @PathVariable Long strategyId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                mappingService.getCoverageReport(strategyId, principal.getId())));
    }

    // Scoped by the CALLING department strategy (not the planning cycle directly) so the
    // permission check is against a strategy the caller actually has a role on -- a department
    // editor mapping their own objectives has no reason to hold any role on the university
    // strategy itself.
    @GetMapping("/strategies/{deptStrategyId}/university-objectives")
    public ResponseEntity<ApiResponse<List<ObjectiveResponse>>> getUniversityObjectives(
            @PathVariable Long deptStrategyId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                mappingService.getAvailableUniversityObjectives(deptStrategyId, principal.getId())));
    }

    @GetMapping("/objectives/{deptObjectiveId}/available-university-initiatives")
    public ResponseEntity<ApiResponse<List<InitiativeResponse>>> getAvailableUniversityInitiatives(
            @PathVariable Long deptObjectiveId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                mappingService.getAvailableUniversityInitiatives(deptObjectiveId, principal.getId())));
    }
}
