package com.rit.spms.controller;

import com.rit.spms.domain.PortfolioEntry;
import com.rit.spms.dto.request.CreateAchievementRequest;
import com.rit.spms.dto.response.ApiResponse;
import com.rit.spms.security.UserPrincipal;
import com.rit.spms.service.PortfolioEntryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/** Achievement + portfolio-evaluation logging (category/criteria/rating/goal), one action creating both records -- see {@link PortfolioEntryService}. */
@RestController
@RequestMapping("/api/portfolio/entries")
@RequiredArgsConstructor
public class PortfolioEntryController {

    private final PortfolioEntryService entryService;

    // Entry Creation & Management

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PortfolioEntryResponse>> logAchievement(
            @Valid @RequestBody LogAchievementRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        CreateAchievementRequest achievementReq = new CreateAchievementRequest();
        achievementReq.setTitle(req.getAchievementTitle());
        achievementReq.setAchievementTypeId(req.getAchievementTypeId());
        achievementReq.setCustomTypeName(req.getCustomTypeName());
        achievementReq.setDetails(req.getDetails());
        achievementReq.setPrivateNotes(req.getPrivateNotes());
        achievementReq.setAssessmentPeriodId(req.getAssessmentPeriodId());

        PortfolioEntry entry = entryService.logAchievementWithEvaluation(
                req.getMeasurementId(), achievementReq, req.getCategoryId(), req.getCriteriaId(), req.getCategoryRating(),
                req.getGoalId(), req.getEvidenceUrl(), principal.getId());
        return ResponseEntity.status(201).body(ApiResponse.success("Achievement logged", map(entry)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PortfolioEntryResponse>> getEntry(@PathVariable Long id) {
        PortfolioEntry entry = entryService.getEntryById(id);
        return ResponseEntity.ok(ApiResponse.success(map(entry)));
    }

    @GetMapping("/my-portfolio")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PortfolioEntryResponse>>> getMyPortfolio(
            @RequestParam(required = false) Long academicYearId,
            @AuthenticationPrincipal UserPrincipal principal) {
        List<PortfolioEntryResponse> entries = entryService.getEmployeePortfolio(principal.getId(), academicYearId, principal.getId())
                .stream().map(this::map).toList();
        return ResponseEntity.ok(ApiResponse.success(entries));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PortfolioEntryResponse>>> getEmployeePortfolio(
            @PathVariable Long employeeId, @RequestParam(required = false) Long academicYearId,
            @AuthenticationPrincipal UserPrincipal principal) {
        List<PortfolioEntryResponse> entries = entryService.getEmployeePortfolio(employeeId, academicYearId, principal.getId())
                .stream().map(this::map).toList();
        return ResponseEntity.ok(ApiResponse.success(entries));
    }

    @GetMapping("/my-portfolio/by-category/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PortfolioEntryResponse>>> getPortfolioByCategory(
            @PathVariable Long categoryId,
            @AuthenticationPrincipal UserPrincipal principal) {
        List<PortfolioEntryResponse> entries = entryService.getEmployeePortfolioByCategory(principal.getId(), categoryId, principal.getId())
                .stream().map(this::map).toList();
        return ResponseEntity.ok(ApiResponse.success(entries));
    }

    @GetMapping("/my-portfolio/by-goal/{goalId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PortfolioEntryResponse>>> getPortfolioByGoal(
            @PathVariable Long goalId,
            @AuthenticationPrincipal UserPrincipal principal) {
        List<PortfolioEntryResponse> entries = entryService.getEmployeePortfolioByGoal(principal.getId(), goalId, principal.getId())
                .stream().map(this::map).toList();
        return ResponseEntity.ok(ApiResponse.success(entries));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PortfolioEntryResponse>> updateEntry(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAchievementRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        PortfolioEntry entry = entryService.updateEntry(id, req.getCriteriaId(), req.getCategoryRating(), req.getGoalId(), req.getEvidenceUrl(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Achievement updated", map(entry)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteEntry(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        entryService.deleteEntry(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Achievement deleted", null));
    }

    // Achievement-linked entries (Strategy Tree's achievement-recording modal)

    @GetMapping("/by-achievement/{achievementId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PortfolioEntryResponse>> getEntryByAchievement(
            @PathVariable Long achievementId, @AuthenticationPrincipal UserPrincipal principal) {
        PortfolioEntry entry = entryService.getEntryByAchievementId(achievementId, principal.getId());
        if (entry == null) {
            return ResponseEntity.ok(ApiResponse.success(null));
        }
        PortfolioEntryResponse resp = map(entry);
        // Self-assessment rating, evidence link, and Annual Goal are personal to the employee --
        // only the achievement's own author or someone in their org hierarchy (their head, or any
        // head above that head) may see them; every other strategy member sees the achievement
        // itself but not this evaluation-facing detail.
        if (!entryService.canViewSensitiveFields(achievementId, principal.getId())) {
            resp.setCategoryRating(null);
            resp.setEvidenceUrl(null);
            resp.setGoalId(null);
            resp.setGoalTitle(null);
        }
        return ResponseEntity.ok(ApiResponse.success(resp));
    }

    @GetMapping("/by-measurement/{measurementId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PortfolioEntryResponse>>> getEntriesByMeasurement(
            @PathVariable Long measurementId, @AuthenticationPrincipal UserPrincipal principal) {
        List<PortfolioEntryResponse> entries = entryService.getEntriesByMeasurementId(measurementId, principal.getId())
                .stream().map(this::map).toList();
        return ResponseEntity.ok(ApiResponse.success(entries));
    }

    @PutMapping("/by-achievement/{achievementId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PortfolioEntryResponse>> upsertEntryForAchievement(
            @PathVariable Long achievementId,
            @Valid @RequestBody UpsertEvaluationRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        PortfolioEntry entry = entryService.upsertEvaluationForAchievement(
                achievementId, req.getCategoryId(), req.getCriteriaId(), req.getCategoryRating(), req.getGoalId(), req.getEvidenceUrl(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Evaluation updated", map(entry)));
    }

    // Linking & Management

    @PutMapping("/{entryId}/link-goal/{goalId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> linkToGoal(
            @PathVariable Long entryId,
            @PathVariable Long goalId,
            @AuthenticationPrincipal UserPrincipal principal) {
        entryService.linkEntryToGoal(entryId, goalId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Entry linked to goal", null));
    }

    // Portfolio Summary & Statistics

    @GetMapping("/my-portfolio/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PortfolioSummaryResponse>> getPortfolioSummary(
            @RequestParam Long academicYearId,
            @AuthenticationPrincipal UserPrincipal principal) {
        PortfolioEntryService.PortfolioSummary summary = entryService.getEmployeePortfolioSummary(principal.getId(), academicYearId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(mapSummary(summary)));
    }

    @GetMapping("/employee/{employeeId}/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PortfolioSummaryResponse>> getEmployeePortfolioSummary(
            @PathVariable Long employeeId,
            @RequestParam Long academicYearId,
            @AuthenticationPrincipal UserPrincipal principal) {
        PortfolioEntryService.PortfolioSummary summary = entryService.getEmployeePortfolioSummary(employeeId, academicYearId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(mapSummary(summary)));
    }

    // DTOs & Mappers

    @lombok.Data
    public static class PortfolioEntryResponse {
        private Long id;
        private Long achievementId;
        private Long employeeId;
        private Long measurementId;
        private String achievementTitle;
        private String achievementDetails;
        private String achievementTypeName;
        private Long categoryId;
        private String categoryName;
        private Long criteriaId;
        private String criteriaName;
        private Long goalId;
        private String goalTitle;
        private Integer categoryRating;
        private String evidenceUrl;
        private LocalDateTime recordedAt;
    }

    @lombok.Data
    @lombok.Builder
    public static class PortfolioSummaryResponse {
        private Long employeeId;
        private String employeeName;
        private int totalEntries;
        private int deployedGoals;
        private double averageRating;
    }

    @lombok.Data
    public static class LogAchievementRequest {
        @NotNull private Long measurementId;
        @NotBlank private String achievementTitle;
        @NotNull private Long achievementTypeId;
        private String customTypeName;
        private String details;
        private String privateNotes;
        private Long assessmentPeriodId;
        @NotNull private Long categoryId;
        private Long criteriaId;
        private Integer categoryRating;
        private Long goalId;
        @NotBlank(message = "Evidence/Link is required") private String evidenceUrl;
    }

    @lombok.Data
    public static class UpdateAchievementRequest {
        private Long criteriaId;
        private Integer categoryRating;
        private Long goalId;
        @NotBlank(message = "Evidence/Link is required") private String evidenceUrl;
    }

    @lombok.Data
    public static class UpsertEvaluationRequest {
        @NotNull private Long categoryId;
        private Long criteriaId;
        private Integer categoryRating;
        private Long goalId;
        @NotBlank(message = "Evidence/Link is required") private String evidenceUrl;
    }

    private PortfolioEntryResponse map(PortfolioEntry entry) {
        PortfolioEntryResponse resp = new PortfolioEntryResponse();
        resp.setId(entry.getId());
        resp.setAchievementId(entry.getAchievement().getId());
        resp.setEmployeeId(entry.getEmployee().getId());
        resp.setMeasurementId(entry.getAchievement().getMeasurement() != null ? entry.getAchievement().getMeasurement().getId() : null);
        resp.setAchievementTitle(entry.getAchievement().getTitle());
        resp.setAchievementDetails(entry.getAchievement().getDetails());
        resp.setAchievementTypeName(entry.getAchievement().getEffectiveTypeName());
        resp.setCategoryId(entry.getCategory().getId());
        resp.setCategoryName(entry.getCategory().getCategoryName());
        if (entry.getCriteria() != null) {
            resp.setCriteriaId(entry.getCriteria().getId());
            resp.setCriteriaName(entry.getCriteria().getCriteriaName());
        }
        if (entry.getGoal() != null) {
            resp.setGoalId(entry.getGoal().getId());
            resp.setGoalTitle(entry.getGoal().getGoalTitle());
        }
        resp.setCategoryRating(entry.getCategoryRating());
        resp.setEvidenceUrl(entry.getEvidenceUrl());
        resp.setRecordedAt(entry.getAchievement().getRecordedAt());
        return resp;
    }

    private PortfolioSummaryResponse mapSummary(PortfolioEntryService.PortfolioSummary summary) {
        return PortfolioSummaryResponse.builder()
                .employeeId(summary.getEmployeeId())
                .employeeName(summary.getEmployeeName())
                .totalEntries(summary.getTotalEntries())
                .deployedGoals(summary.getDeployedGoals())
                .averageRating(summary.getAverageRating())
                .build();
    }
}
