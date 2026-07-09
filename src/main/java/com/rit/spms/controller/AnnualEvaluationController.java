package com.rit.spms.controller;

import com.rit.spms.domain.*;
import com.rit.spms.dto.response.ApiResponse;
import com.rit.spms.security.UserPrincipal;
import com.rit.spms.service.AnnualEvaluationService;
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

/**
 * REST surface for the end-of-year Annual Evaluation workflow: employee self-assessment,
 * head rating (per-criteria, per-category, overall), and the sign/refuse-to-sign step that
 * concludes and freezes the evaluation. See {@link AnnualEvaluationService} for the actual
 * state machine and edit-window rules this controller just exposes.
 */
@RestController
@RequestMapping("/api/portfolio/evaluations")
@RequiredArgsConstructor
public class AnnualEvaluationController {

    private final AnnualEvaluationService evaluationService;

    @GetMapping("/my/{academicYearId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AnnualEvaluationResponse>> getMy(
            @PathVariable Long academicYearId, @AuthenticationPrincipal UserPrincipal principal) {
        AnnualEvaluation evaluation = evaluationService.getOrCreateForEmployeeAndYear(
                principal.getId(), academicYearId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(mapDetail(evaluation, principal.getId())));
    }

    @GetMapping("/team/{academicYearId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<AnnualEvaluationResponse>>> getTeam(
            @PathVariable Long academicYearId, @AuthenticationPrincipal UserPrincipal principal) {
        List<AnnualEvaluationResponse> evaluations = evaluationService.getForHeadAndYear(principal.getId(), academicYearId)
                .stream().map(this::mapSummary).toList();
        return ResponseEntity.ok(ApiResponse.success(evaluations));
    }

    /** Read-only status rollup across a head's whole hierarchy (direct department(s) plus every department under any org group they head). */
    @GetMapping("/hierarchy/{academicYearId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<AnnualEvaluationResponse>>> getHierarchy(
            @PathVariable Long academicYearId, @AuthenticationPrincipal UserPrincipal principal) {
        List<AnnualEvaluationResponse> evaluations = evaluationService.getHierarchyEvaluations(principal.getId(), academicYearId)
                .stream().map(this::mapSummary).toList();
        return ResponseEntity.ok(ApiResponse.success(evaluations));
    }

    /** Admin/HR report search -- org-wide, not scoped to a specific head. */
    @GetMapping("/concluded/{academicYearId}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<ApiResponse<List<AnnualEvaluationResponse>>> getConcluded(@PathVariable Long academicYearId) {
        List<AnnualEvaluationResponse> evaluations = evaluationService.getConcludedForYear(academicYearId)
                .stream().map(this::mapSummary).toList();
        return ResponseEntity.ok(ApiResponse.success(evaluations));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AnnualEvaluationResponse>> getById(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        AnnualEvaluation evaluation = evaluationService.getById(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(mapDetail(evaluation, principal.getId())));
    }

    @PutMapping("/{id}/entries/{entryId}/designation")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> updateDesignation(
            @PathVariable Long id, @PathVariable Long entryId,
            @Valid @RequestBody DesignationRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        evaluationService.updateEntryDesignation(id, entryId, req.getCategoryId(), req.getCriteriaId(), req.getGoalId(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Designation updated", null));
    }

    @PutMapping("/{id}/criteria/{criteriaId}/nothing-to-report")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markCriteriaNothingToReport(
            @PathVariable Long id, @PathVariable Long criteriaId,
            @Valid @RequestBody NothingToReportRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        evaluationService.markCriteriaNothingToReport(id, criteriaId, req.isNothingToReport(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Updated", null));
    }

    @PutMapping("/{id}/goals/{goalId}/nothing-to-report")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markGoalNothingToReport(
            @PathVariable Long id, @PathVariable Long goalId,
            @Valid @RequestBody NothingToReportRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        evaluationService.markGoalNothingToReport(id, goalId, req.isNothingToReport(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Updated", null));
    }

    @PutMapping("/{id}/categories/{categoryId}/self-rank")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> updateSelfRank(
            @PathVariable Long id, @PathVariable Long categoryId,
            @Valid @RequestBody RankRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        evaluationService.updateSelfRank(id, categoryId, req.getRank(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Self-rank updated", null));
    }

    // One self-rank covers the whole Annual Goals section -- parallel to a category's own self-rank.
    @PutMapping("/{id}/goals-self-rank")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> updateGoalsSelfRank(
            @PathVariable Long id, @Valid @RequestBody RankRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        evaluationService.updateGoalsSelfRank(id, req.getRank(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Self-rank updated", null));
    }

    @PostMapping("/{id}/submit-employee")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AnnualEvaluationResponse>> submitEmployee(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        AnnualEvaluation evaluation = evaluationService.submitEmployeeSelfAssessment(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Self-assessment submitted", mapDetail(evaluation, principal.getId())));
    }

    @PutMapping("/{id}/criteria/{criteriaId}/rank")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> updateCriteriaRank(
            @PathVariable Long id, @PathVariable Long criteriaId,
            @Valid @RequestBody RankRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        evaluationService.updateCriteriaRank(id, criteriaId, req.getRank(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Criteria rank updated", null));
    }

    @PutMapping("/{id}/categories/{categoryId}/head-rank")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> updateCategoryHeadRank(
            @PathVariable Long id, @PathVariable Long categoryId,
            @Valid @RequestBody RankRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        evaluationService.updateCategoryHeadRank(id, categoryId, req.getRank(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Category rank updated", null));
    }

    @PutMapping("/{id}/categories/{categoryId}/head-comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> updateCategoryHeadComments(
            @PathVariable Long id, @PathVariable Long categoryId,
            @Valid @RequestBody CommentsRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        evaluationService.updateCategoryHeadComments(id, categoryId, req.getComments(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Category comments updated", null));
    }

    @PutMapping("/{id}/goals/{goalId}/head-rank")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> updateGoalHeadRank(
            @PathVariable Long id, @PathVariable Long goalId,
            @Valid @RequestBody RankRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        evaluationService.updateGoalHeadRank(id, goalId, req.getRank(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Goal rank updated", null));
    }

    // One comment field for the whole Annual Goals section -- parallel to a category's own comment field.
    @PutMapping("/{id}/goals-comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> updateGoalsHeadComments(
            @PathVariable Long id, @Valid @RequestBody CommentsRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        evaluationService.updateGoalsHeadComments(id, req.getComments(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Goals comments updated", null));
    }

    // One head rank for the whole Annual Goals section -- parallel to a category's headCategoryRank.
    @PutMapping("/{id}/goals-head-rank")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> updateGoalsHeadRank(
            @PathVariable Long id, @Valid @RequestBody RankRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        evaluationService.updateGoalsHeadRank(id, req.getRank(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Goals rank updated", null));
    }

    @PutMapping("/{id}/overall-rank")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> updateOverallRank(
            @PathVariable Long id, @Valid @RequestBody RankRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        evaluationService.updateOverallRank(id, req.getRank(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Overall rank updated", null));
    }

    @PostMapping("/{id}/submit-head")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AnnualEvaluationResponse>> submitHead(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        AnnualEvaluation evaluation = evaluationService.submitHeadEvaluation(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Head evaluation submitted", mapDetail(evaluation, principal.getId())));
    }

    @PostMapping("/{id}/sign-head")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AnnualEvaluationResponse>> signHead(
            @PathVariable Long id, @Valid @RequestBody SignRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        AnnualEvaluation evaluation = evaluationService.signAsHead(id, req.getSignatureName(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Signed", mapDetail(evaluation, principal.getId())));
    }

    @PostMapping("/{id}/sign-employee")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AnnualEvaluationResponse>> signEmployee(
            @PathVariable Long id, @Valid @RequestBody SignRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        AnnualEvaluation evaluation = evaluationService.signAsEmployee(id, req.getSignatureName(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Signed", mapDetail(evaluation, principal.getId())));
    }

    @PostMapping("/{id}/refuse-to-sign")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AnnualEvaluationResponse>> refuseToSign(
            @PathVariable Long id, @Valid @RequestBody RefuseRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        AnnualEvaluation evaluation = evaluationService.refuseToSign(id, req.getRationale(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Refusal recorded", mapDetail(evaluation, principal.getId())));
    }

    // ─── DTOs ───────────────────────────────────────────────────────────────────────────

    @lombok.Data
    public static class DesignationRequest {
        @NotNull private Long categoryId;
        private Long criteriaId;
        private Long goalId;
    }

    @lombok.Data
    public static class RankRequest {
        @NotNull private Integer rank;
    }

    @lombok.Data
    public static class CommentsRequest {
        private String comments;
    }

    @lombok.Data
    public static class SignRequest {
        @NotBlank(message = "Type your full name to sign this evaluation")
        private String signatureName;
    }

    @lombok.Data
    public static class NothingToReportRequest {
        private boolean nothingToReport;
    }

    @lombok.Data
    public static class RefuseRequest {
        @NotBlank private String rationale;
    }

    @lombok.Data
    public static class AnnualEvaluationResponse {
        private Long id;
        private Long employeeId;
        private String employeeName;
        private String departmentName;
        private Long headId;
        private String headName;
        private Long academicYearId;
        private String academicYearName;
        private String state;
        private Integer headOverallRank;
        private LocalDateTime employeeSubmittedAt;
        private LocalDateTime headSubmittedAt;
        private LocalDateTime headSignedAt;
        private String headSignatureName;
        private LocalDateTime employeeSignedAt;
        private String employeeSignatureName;
        private Boolean employeeRefused;
        private String employeeRefusalRationale;
        private boolean locked;
        private boolean concluded;
        private Long titleId;
        private List<CategoryResultResponse> categoryResults;
        private List<CriteriaResultResponse> criteriaResults;
        private List<GoalResultResponse> goalResults;
        private String goalsHeadComments;
        private Integer goalsEmployeeSelfRank;
        private Integer goalsHeadRank;
        private List<EntryResponse> entries;
    }

    @lombok.Data
    public static class CategoryResultResponse {
        private Long categoryId;
        private String categoryName;
        private Integer employeeSelfRank;
        private Integer headCategoryRank;
        private String headComments;
    }

    @lombok.Data
    public static class CriteriaResultResponse {
        private Long criteriaId;
        private String criteriaName;
        private Long categoryId;
        private Integer headRank;
        private Boolean employeeNothingToReport;
        private String rubricUnsatisfactory;
        private String rubricMeetsExpectations;
        private String rubricExceedsExpectations;
    }

    @lombok.Data
    public static class GoalResultResponse {
        private Long goalId;
        private String goalTitle;
        private Boolean employeeNothingToReport;
        private Integer headGoalRank;
        private String rubricUnsatisfactory;
        private String rubricMeetsExpectations;
        private String rubricExceedsExpectations;
    }

    @lombok.Data
    public static class EntryResponse {
        private Long entryId;
        private Long achievementId;
        private String achievementTitle;
        private Long categoryId;
        private Long criteriaId;
        private Long goalId;
    }

    // ─── Mapping ────────────────────────────────────────────────────────────────────────

    private AnnualEvaluationResponse mapSummary(AnnualEvaluation e) {
        AnnualEvaluationResponse resp = new AnnualEvaluationResponse();
        resp.setId(e.getId());
        resp.setEmployeeId(e.getEmployee().getId());
        resp.setEmployeeName(e.getEmployee().getFname() + " " + e.getEmployee().getLname());
        resp.setDepartmentName(e.getEmployee().getDepartment() != null ? e.getEmployee().getDepartment().getName() : null);
        resp.setHeadId(e.getHead().getId());
        resp.setHeadName(e.getHead().getFname() + " " + e.getHead().getLname());
        resp.setAcademicYearId(e.getAcademicYear().getId());
        resp.setAcademicYearName(e.getAcademicYear().getName());
        resp.setState(e.getState().name());
        resp.setHeadOverallRank(e.getHeadOverallRank());
        resp.setGoalsHeadComments(e.getGoalsHeadComments());
        resp.setGoalsEmployeeSelfRank(e.getGoalsEmployeeSelfRank());
        resp.setGoalsHeadRank(e.getGoalsHeadRank());
        resp.setEmployeeSubmittedAt(e.getEmployeeSubmittedAt());
        resp.setHeadSubmittedAt(e.getHeadSubmittedAt());
        resp.setHeadSignedAt(e.getHeadSignedAt());
        resp.setHeadSignatureName(e.getHeadSignatureName());
        resp.setEmployeeSignedAt(e.getEmployeeSignedAt());
        resp.setEmployeeSignatureName(e.getEmployeeSignatureName());
        resp.setEmployeeRefused(e.getEmployeeRefused());
        resp.setEmployeeRefusalRationale(e.getEmployeeRefusalRationale());
        resp.setLocked(e.isLocked());
        resp.setConcluded(e.isConcluded());
        return resp;
    }

    private AnnualEvaluationResponse mapDetail(AnnualEvaluation e, Long currentUserId) {
        AnnualEvaluationResponse resp = mapSummary(e);
        List<com.rit.spms.domain.AnnualEvaluationCategoryResult> categoryResults = evaluationService.getCategoryResults(e.getId());
        if (!categoryResults.isEmpty()) {
            resp.setTitleId(categoryResults.get(0).getCategory().getTitle().getId());
        }
        resp.setCategoryResults(categoryResults.stream().map(r -> {
            CategoryResultResponse c = new CategoryResultResponse();
            c.setCategoryId(r.getCategory().getId());
            c.setCategoryName(r.getCategory().getCategoryName());
            c.setEmployeeSelfRank(r.getEmployeeSelfRank());
            c.setHeadCategoryRank(r.getHeadCategoryRank());
            c.setHeadComments(r.getHeadComments());
            return c;
        }).toList());
        resp.setCriteriaResults(evaluationService.getCriteriaResults(e.getId()).stream().map(r -> {
            CriteriaResultResponse c = new CriteriaResultResponse();
            c.setCriteriaId(r.getCriteria().getId());
            c.setCriteriaName(r.getCriteria().getCriteriaName());
            c.setCategoryId(r.getCriteria().getCategory().getId());
            c.setHeadRank(r.getHeadRank());
            c.setEmployeeNothingToReport(r.getEmployeeNothingToReport());
            c.setRubricUnsatisfactory(r.getCriteria().getRubricUnsatisfactory());
            c.setRubricMeetsExpectations(r.getCriteria().getRubricMeetsExpectations());
            c.setRubricExceedsExpectations(r.getCriteria().getRubricExceedsExpectations());
            return c;
        }).toList());
        resp.setGoalResults(evaluationService.getGoalResults(e.getId()).stream().map(r -> {
            GoalResultResponse g = new GoalResultResponse();
            g.setGoalId(r.getGoal().getId());
            g.setGoalTitle(r.getGoal().getGoalTitle());
            g.setEmployeeNothingToReport(r.getEmployeeNothingToReport());
            g.setHeadGoalRank(r.getHeadGoalRank());
            g.setRubricUnsatisfactory(r.getGoal().getRubricUnsatisfactory());
            g.setRubricMeetsExpectations(r.getGoal().getRubricMeetsExpectations());
            g.setRubricExceedsExpectations(r.getGoal().getRubricExceedsExpectations());
            return g;
        }).toList());
        resp.setEntries(evaluationService.getEntriesInPeriod(e.getId(), currentUserId).stream().map(entry -> {
            EntryResponse r = new EntryResponse();
            r.setEntryId(entry.getId());
            r.setAchievementId(entry.getAchievement().getId());
            r.setAchievementTitle(entry.getAchievement().getTitle());
            r.setCategoryId(entry.getCategory().getId());
            if (entry.getCriteria() != null) {
                r.setCriteriaId(entry.getCriteria().getId());
            }
            if (entry.getGoal() != null) {
                r.setGoalId(entry.getGoal().getId());
            }
            return r;
        }).toList());

        // An achievement tagged to a criteria/goal makes a stored "nothing to report" flag stale --
        // compute the effective value here (rather than relying on every write path that can set an
        // entry's criteria/goal to remember to clear it) so the UI never shows a checked-but-disabled
        // contradiction.
        resp.getCriteriaResults().forEach(c -> {
            if (resp.getEntries().stream().anyMatch(en -> c.getCriteriaId().equals(en.getCriteriaId()))) {
                c.setEmployeeNothingToReport(false);
            }
        });
        resp.getGoalResults().forEach(g -> {
            if (resp.getEntries().stream().anyMatch(en -> g.getGoalId().equals(en.getGoalId()))) {
                g.setEmployeeNothingToReport(false);
            }
        });
        return resp;
    }
}
