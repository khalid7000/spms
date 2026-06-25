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

    @GetMapping("/planning-cycles/{cycleId}/university-objectives")
    public ResponseEntity<ApiResponse<List<ObjectiveResponse>>> getUniversityObjectives(
            @PathVariable Long cycleId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                mappingService.getAvailableUniversityObjectives(cycleId, principal.getId())));
    }

    @GetMapping("/objectives/{deptObjectiveId}/available-university-initiatives")
    public ResponseEntity<ApiResponse<List<InitiativeResponse>>> getAvailableUniversityInitiatives(
            @PathVariable Long deptObjectiveId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                mappingService.getAvailableUniversityInitiatives(deptObjectiveId, principal.getId())));
    }
}
