package com.rit.spms.controller;

import com.rit.spms.domain.Achievement;
import com.rit.spms.dto.request.CreateAchievementRequest;
import com.rit.spms.dto.response.AchievementResponse;
import com.rit.spms.dto.response.ApiResponse;
import com.rit.spms.security.UserPrincipal;
import com.rit.spms.service.AchievementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;

    @GetMapping("/api/measurements/{measurementId}/achievements")
    public ResponseEntity<ApiResponse<List<AchievementResponse>>> getAchievements(
            @PathVariable Long measurementId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                achievementService.getAchievements(measurementId, principal.getId())));
    }

    @PostMapping("/api/measurements/{measurementId}/achievements")
    public ResponseEntity<ApiResponse<AchievementResponse>> recordAchievement(
            @PathVariable Long measurementId,
            @Valid @RequestBody CreateAchievementRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        Achievement achievement = achievementService.recordAchievement(measurementId, req, principal.getId());
        return ResponseEntity.status(201).body(
                ApiResponse.success("Achievement recorded", achievementService.toResponse(achievement, principal.getId())));
    }

    @PutMapping("/api/achievements/{id}")
    public ResponseEntity<ApiResponse<AchievementResponse>> updateAchievement(
            @PathVariable Long id,
            @Valid @RequestBody CreateAchievementRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        Achievement achievement = achievementService.updateAchievement(id, req, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(achievementService.toResponse(achievement, principal.getId())));
    }

    @DeleteMapping("/api/achievements/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAchievement(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        achievementService.deleteAchievement(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Achievement deleted", null));
    }

    @GetMapping("/api/initiatives/{initiativeId}/achievements/across-years")
    public ResponseEntity<ApiResponse<List<AchievementResponse>>> getAchievementsAcrossYears(
            @PathVariable Long initiativeId,
            @RequestParam(required = false) String periodName,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                achievementService.getAchievementsAcrossYears(initiativeId, periodName, principal.getId())));
    }

    @GetMapping("/api/initiatives/{initiativeId}/aggregated-achievements")
    public ResponseEntity<ApiResponse<List<AchievementResponse>>> getAggregatedAchievements(
            @PathVariable Long initiativeId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                achievementService.getAggregatedAchievements(initiativeId, principal.getId())));
    }
}
