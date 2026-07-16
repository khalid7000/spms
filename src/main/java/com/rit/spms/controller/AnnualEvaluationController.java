package com.rit.spms.controller;

import com.rit.spms.domain.*;
import com.rit.spms.domain.enums.PortfolioReviewActionType;
import com.rit.spms.dto.response.ApiResponse;
import com.rit.spms.security.UserPrincipal;
import com.rit.spms.repository.CriteriaAchievementModuleRepository;
import com.rit.spms.repository.CriteriaInfoToolAssignmentRepository;
import com.rit.spms.service.AchievementService;
import com.rit.spms.service.AnnualEvaluationNextCycleGoalService;
import com.rit.spms.service.AnnualEvaluationService;
import com.rit.spms.service.CriteriaInfoTool;
import com.rit.spms.service.CriteriaInfoToolRegistry;
import com.rit.spms.service.PermissionService;
import com.rit.spms.service.RatingAssistantSelectionService;
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
 * head rating (per-criteria, per-category, overall) followed by a combined submit-and-sign,
 * an optional one-time return-to-employee round in between, and the employee's own
 * sign/refuse-to-sign step that concludes and freezes the evaluation. See {@link
 * AnnualEvaluationService} for the actual state machine and edit-window rules this controller
 * just exposes.
 */
@RestController
@RequestMapping("/api/portfolio/evaluations")
@RequiredArgsConstructor
public class AnnualEvaluationController {

    private final AnnualEvaluationService evaluationService;
    private final AnnualEvaluationNextCycleGoalService nextCycleGoalService;
    private final CriteriaAchievementModuleRepository achievementModuleRepository;
    private final RatingAssistantSelectionService ratingAssistantSelectionService;
    private final AchievementService achievementService;
    private final CriteriaInfoToolAssignmentRepository infoToolAssignmentRepository;
    private final CriteriaInfoToolRegistry infoToolRegistry;
    private final PermissionService permissionService;

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

    // ─── Criteria Info Tools (head-only viewer) ────────────────────────────────────────

    @lombok.Data
    public static class InfoOptionResponse {
        private String key;
        private String label;
    }

    @GetMapping("/{id}/criteria/{criteriaId}/info-tool/options")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<InfoOptionResponse>>> getInfoToolOptions(
            @PathVariable Long id, @PathVariable Long criteriaId,
            @RequestParam(required = false) String repositorySourceType,
            @AuthenticationPrincipal UserPrincipal principal) {
        AnnualEvaluation evaluation = evaluationService.getById(id, principal.getId());
        permissionService.assertCanUseCriteriaInfoTool(principal.getId(), evaluation);
        CriteriaInfoToolAssignment assignment = requireInfoToolAssignment(criteriaId, repositorySourceType);
        CriteriaInfoTool tool = infoToolRegistry.require(assignment.getToolCode());
        AppUser employee = evaluation.getEmployee();
        List<InfoOptionResponse> options = tool.listAvailableOptions(assignment, employee.getFname(), employee.getLname(), employee.getEmail())
                .stream().map(o -> {
                    InfoOptionResponse resp = new InfoOptionResponse();
                    resp.setKey(o.key());
                    resp.setLabel(o.label());
                    return resp;
                }).toList();
        return ResponseEntity.ok(ApiResponse.success(options));
    }

    @GetMapping("/{id}/criteria/{criteriaId}/info-tool/details")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> getInfoToolDetails(
            @PathVariable Long id, @PathVariable Long criteriaId, @RequestParam List<String> terms,
            @RequestParam(required = false) String repositorySourceType,
            @AuthenticationPrincipal UserPrincipal principal) {
        AnnualEvaluation evaluation = evaluationService.getById(id, principal.getId());
        permissionService.assertCanUseCriteriaInfoTool(principal.getId(), evaluation);
        CriteriaInfoToolAssignment assignment = requireInfoToolAssignment(criteriaId, repositorySourceType);
        CriteriaInfoTool tool = infoToolRegistry.require(assignment.getToolCode());
        AppUser employee = evaluation.getEmployee();
        String details = tool.getDetails(assignment, employee.getFname(), employee.getLname(), employee.getEmail(), terms);
        return ResponseEntity.ok(ApiResponse.success(details));
    }

    // A criterion can carry more than one info tool assignment (e.g. both an Early-Alert-flavored
    // and a Grade-Distribution-flavored Central Repository Viewer), so repositorySourceType
    // disambiguates which one this request is for.
    private CriteriaInfoToolAssignment requireInfoToolAssignment(Long criteriaId, String repositorySourceType) {
        return infoToolAssignmentRepository.findByCriteriaId(criteriaId).stream()
                .filter(a -> java.util.Objects.equals(a.getRepositorySourceType(), repositorySourceType))
                .findFirst()
                .orElseThrow(() -> new com.rit.spms.exception.BusinessRuleException("No info tool is assigned to this criteria"));
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
        evaluationService.updateCategoryHeadComments(id, categoryId, req.getStrengths(), req.getImprovements(), principal.getId());
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
        evaluationService.updateGoalsHeadComments(id, req.getStrengths(), req.getImprovements(), principal.getId());
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

    // ─── Next Cycle Goals ───────────────────────────────────────────────────
    // Drafted/reviewed by both head and employee during THIS evaluation's own review/sign
    // exchange -- see AnnualEvaluationNextCycleGoalService for the exact mechanics (mirrors
    // Team Goal Setting's AI-suggestion generation, but stays scoped to this evaluation).

    @PutMapping("/{id}/next-cycle-goals/notes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> updateNextCycleGoalNotes(
            @PathVariable Long id, @RequestBody NextCycleGoalNotesRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        nextCycleGoalService.updateNotes(id, req.getStrengths(), req.getWeaknesses(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Notes saved", null));
    }

    // Fire-and-forget, identical shape to EmployeeGoalCycleController.generateSuggestions -- see
    // the comment there for why the checkpoint + @Async kickoff must both happen directly here.
    @PostMapping("/{id}/next-cycle-goals/generate-suggestions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AnnualEvaluationResponse>> generateNextCycleGoalSuggestions(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        List<PortfolioCategory> categories = nextCycleGoalService.assertCanGenerateSuggestions(id, principal.getId());
        nextCycleGoalService.recordGenerationRequested(id);
        nextCycleGoalService.generateSuggestionsAsync(id, categories);
        return ResponseEntity.status(202).body(ApiResponse.success("AI suggestion generation started",
                mapDetail(nextCycleGoalService.requireEvaluation(id), principal.getId())));
    }

    @GetMapping("/{id}/next-cycle-goals")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<NextCycleGoalResponse>>> getNextCycleGoals(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        List<NextCycleGoalResponse> goals = nextCycleGoalService.getGoals(id, principal.getId())
                .stream().map(this::map).toList();
        return ResponseEntity.ok(ApiResponse.success(goals));
    }

    @PostMapping("/{id}/next-cycle-goals")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<NextCycleGoalResponse>> addNextCycleGoal(
            @PathVariable Long id, @Valid @RequestBody AddNextCycleGoalRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        AnnualEvaluationNextCycleGoal goal = nextCycleGoalService.addGoal(id, req.getCategoryId(), req.getTitle(), req.getDescription(),
                req.getRubricUnsatisfactory(), req.getRubricMeetsExpectations(), req.getRubricExceedsExpectations(), principal.getId());
        return ResponseEntity.status(201).body(ApiResponse.success(map(goal)));
    }

    @PutMapping("/{id}/next-cycle-goals/{goalId}/rubric")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<NextCycleGoalResponse>> updateNextCycleGoalRubric(
            @PathVariable Long id, @PathVariable Long goalId,
            @RequestBody UpdateNextCycleGoalRubricRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        AnnualEvaluationNextCycleGoal goal = nextCycleGoalService.updateRubric(id, goalId,
                req.getRubricUnsatisfactory(), req.getRubricMeetsExpectations(), req.getRubricExceedsExpectations(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success(map(goal)));
    }

    @PutMapping("/{id}/next-cycle-goals/{goalId}/leader-review")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<NextCycleGoalResponse>> reviewNextCycleGoalAsLeader(
            @PathVariable Long id, @PathVariable Long goalId,
            @Valid @RequestBody NextCycleGoalReviewRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        AnnualEvaluationNextCycleGoal goal = nextCycleGoalService.leaderReview(
                id, goalId, req.getActionType(), req.getEditedTitle(), req.getEditedDescription(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success(map(goal)));
    }

    @PutMapping("/{id}/next-cycle-goals/{goalId}/employee-review")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<NextCycleGoalResponse>> reviewNextCycleGoalAsEmployee(
            @PathVariable Long id, @PathVariable Long goalId,
            @Valid @RequestBody NextCycleGoalReviewRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        AnnualEvaluationNextCycleGoal goal = nextCycleGoalService.employeeReview(
                id, goalId, req.getActionType(), req.getEditedTitle(), req.getEditedDescription(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success(map(goal)));
    }

    @DeleteMapping("/{id}/next-cycle-goals/{goalId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteNextCycleGoal(
            @PathVariable Long id, @PathVariable Long goalId, @AuthenticationPrincipal UserPrincipal principal) {
        nextCycleGoalService.deleteGoal(id, goalId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Next cycle goal removed", null));
    }

    // Rating Assistant word selections -- see RatingAssistantSelectionService. Strictly private to
    // this evaluation's own head; the service itself enforces this regardless of who calls it.
    @GetMapping("/{id}/rating-assistant-selection")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<RatingAssistantSelectionResponse>> getRatingAssistantSelection(
            @PathVariable Long id, @RequestParam String targetType, @RequestParam Long targetId,
            @AuthenticationPrincipal UserPrincipal principal) {
        List<String> history = ratingAssistantSelectionService.getSelectionHistory(id, targetType, targetId, principal.getId());
        RatingAssistantSelectionResponse resp = new RatingAssistantSelectionResponse();
        resp.setSelectionHistory(history);
        return ResponseEntity.ok(ApiResponse.success(resp));
    }

    @PutMapping("/{id}/rating-assistant-selection")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> saveRatingAssistantSelection(
            @PathVariable Long id, @Valid @RequestBody SaveRatingAssistantSelectionRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        ratingAssistantSelectionService.saveSelectionHistory(id, req.getTargetType(), req.getTargetId(), req.getSelectionHistory(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Selection saved", null));
    }

    @PutMapping("/{id}/overall-rank")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> updateOverallRank(
            @PathVariable Long id, @Valid @RequestBody RankRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        evaluationService.updateOverallRank(id, req.getRank(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Overall rank updated", null));
    }

    @PostMapping("/{id}/submit-and-sign-head")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AnnualEvaluationResponse>> submitAndSignHead(
            @PathVariable Long id, @Valid @RequestBody SignRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        AnnualEvaluation evaluation = evaluationService.submitAndSignHeadEvaluation(id, req.getSignatureName(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Evaluation signed and submitted", mapDetail(evaluation, principal.getId())));
    }

    /** Sends the evaluation back to the employee for one more round of edits/comments -- see {@link AnnualEvaluationService#returnToEmployeeForReview}. */
    @PostMapping("/{id}/return-to-employee")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AnnualEvaluationResponse>> returnToEmployee(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        AnnualEvaluation evaluation = evaluationService.returnToEmployeeForReview(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Returned to employee for review and update", mapDetail(evaluation, principal.getId())));
    }

    @PutMapping("/{id}/categories/{categoryId}/employee-comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> updateCategoryEmployeeComments(
            @PathVariable Long id, @PathVariable Long categoryId,
            @RequestBody EmployeeCommentsRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        evaluationService.updateCategoryEmployeeComments(id, categoryId, req.getComments(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Comments saved", null));
    }

    @PutMapping("/{id}/criteria/{criteriaId}/employee-comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> updateCriteriaEmployeeComments(
            @PathVariable Long id, @PathVariable Long criteriaId,
            @RequestBody EmployeeCommentsRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        evaluationService.updateCriteriaEmployeeComments(id, criteriaId, req.getComments(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Comments saved", null));
    }

    // One comment field for the whole Annual Goals section -- parallel to a category's own employee-comments field.
    @PutMapping("/{id}/goals-employee-comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> updateGoalsEmployeeComments(
            @PathVariable Long id, @RequestBody EmployeeCommentsRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        evaluationService.updateGoalsEmployeeComments(id, req.getComments(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Comments saved", null));
    }

    // The employee's required whole-evaluation closing statement -- distinct from the per-category/goals reflections.
    @PutMapping("/{id}/employee-final-summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> updateEmployeeFinalSummary(
            @PathVariable Long id, @RequestBody EmployeeCommentsRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        evaluationService.updateEmployeeFinalSummary(id, req.getComments(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Comments saved", null));
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
    public static class SaveRatingAssistantSelectionRequest {
        @NotBlank private String targetType;
        @NotNull private Long targetId;
        private List<String> selectionHistory;
    }

    @lombok.Data
    public static class RatingAssistantSelectionResponse {
        private List<String> selectionHistory;
    }

    @lombok.Data
    public static class CommentsRequest {
        private String strengths;
        private String improvements;
    }

    @lombok.Data
    public static class EmployeeCommentsRequest {
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
    public static class NextCycleGoalNotesRequest {
        private String strengths;
        private String weaknesses;
    }

    @lombok.Data
    public static class AddNextCycleGoalRequest {
        @NotNull private Long categoryId;
        private String title;
        private String description;
        private String rubricUnsatisfactory;
        private String rubricMeetsExpectations;
        private String rubricExceedsExpectations;
    }

    @lombok.Data
    public static class UpdateNextCycleGoalRubricRequest {
        private String rubricUnsatisfactory;
        private String rubricMeetsExpectations;
        private String rubricExceedsExpectations;
    }

    @lombok.Data
    public static class NextCycleGoalReviewRequest {
        @NotNull private PortfolioReviewActionType actionType;
        private String editedTitle;
        private String editedDescription;
    }

    @lombok.Data
    public static class NextCycleGoalResponse {
        private Long id;
        private Long categoryId;
        private String categoryName;
        private String suggestedTitle;
        private String suggestedDescription;
        private String rationale;
        private String generatedByModel;
        private Integer sortOrder;
        private String rubricUnsatisfactory;
        private String rubricMeetsExpectations;
        private String rubricExceedsExpectations;
        private PortfolioReviewActionType leaderActionType;
        private String leaderEditedTitle;
        private String leaderEditedDescription;
        private PortfolioReviewActionType employeeActionType;
        private String employeeEditedTitle;
        private String employeeEditedDescription;
        private boolean used;
    }

    @lombok.Data
    public static class AnnualEvaluationResponse {
        private Long id;
        private Long employeeId;
        private String employeeName;
        private String departmentName;
        // What to call the head/supervisor for this employee's department -- their department's
        // own configured title, falling back to its org group's, or null if neither is set (the
        // frontend applies the org-wide default label in that case). Not necessarily this specific
        // evaluation's rater -- just a display label.
        private String headTitle;
        private Long headId;
        private String headName;
        private Long academicYearId;
        private String academicYearName;
        private String state;
        private Integer headOverallRank;
        private LocalDateTime employeeSubmittedAt;
        private LocalDateTime returnedToEmployeeAt;
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
        private String goalsHeadCommentsStrengths;
        private String goalsHeadCommentsImprovements;
        private String goalsEmployeeComments;
        private String employeeFinalSummary;
        private Integer goalsEmployeeSelfRank;
        private Integer goalsHeadRank;
        private List<EntryResponse> entries;
        private String nextCycleNotesStrengths;
        private String nextCycleNotesWeaknesses;
        private LocalDateTime nextCycleGenerationRequestedAt;
        private LocalDateTime nextCycleGeneratedAt;
        private String nextCycleGenerationFailureReason;
    }

    @lombok.Data
    public static class CategoryResultResponse {
        private Long categoryId;
        private String categoryName;
        private Integer sortOrder;
        private Integer employeeSelfRank;
        private Integer headCategoryRank;
        private String headCommentsStrengths;
        private String headCommentsImprovements;
        private String employeeComments;
    }

    @lombok.Data
    public static class CriteriaResultResponse {
        private Long criteriaId;
        private String criteriaName;
        private Long categoryId;
        private Integer headRank;
        private Boolean employeeNothingToReport;
        private String employeeComments;
        private String rubricUnsatisfactory;
        private String rubricMeetsExpectations;
        private String rubricExceedsExpectations;
        private List<String> achievementModuleCodes;
        // moduleCode -> the admin-configured max achievements per academic year for that module on
        // this criterion -- lets the employee's page disable a module's launch button once they've
        // hit it (the count itself comes from filtering `entries` by criteriaId+createdByModuleCode).
        private java.util.Map<String, Integer> achievementModuleMaxPerYear;
        // moduleCode -> whether at least one achievement from that module is required for this
        // criterion before the employee can submit (see AnnualEvaluationService.submitEmployeeSelfAssessment).
        private java.util.Map<String, Boolean> achievementModuleMandatory;
        // moduleCode -> admin-set override for the button label, only present when one has actually
        // been set -- absence means "use the module's own hardcoded buttonLabel" (today's behavior).
        private java.util.Map<String, String> achievementModuleDisplayNames;
        // Head-only viewer tool(s) assigned here -- a criterion can carry more than one (e.g. both
        // an Early-Alert-flavored and a Grade-Distribution-flavored Central Repository Viewer).
        // Returned to every viewer including the employee (harmless metadata, a tool name) -- the
        // actual data is never included in this response regardless of viewer, since
        // PermissionService.assertCanUseCriteriaInfoTool gates that behind a dedicated endpoint
        // that explicitly excludes the evaluation's own employee.
        private List<InfoToolAssignmentSummary> infoToolAssignments;
    }

    @lombok.Data
    public static class InfoToolAssignmentSummary {
        private String toolCode;
        private String displayName;
        private String repositorySourceType;
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
        // Shown on hover in the Annual Evaluation display, same as the Strategy Tree's achievement
        // cards -- and everything else here (type, period, author, canEdit/canDelete) needed to
        // support editing/deleting the achievement directly from this page, not just the Strategy
        // Tree -- achievements generated by a criterion-assigned achievement tool (e.g. Teaching
        // Evaluations) have no measurement/Initiative at all, so this page is the ONLY place they
        // can ever be edited.
        private String achievementDetails;
        private Long achievementTypeId;
        private String achievementTypeName;
        private String customTypeName;
        private String privateNotes;
        private Long authorId;
        private String authorName;
        private Long assessmentPeriodId;
        private String assessmentPeriodName;
        private java.time.LocalDateTime recordedAt;
        private boolean canEdit;
        private boolean canDelete;
        private Long categoryId;
        private Long criteriaId;
        private Long goalId;
        // Non-null when an achievement-recording tool (e.g. Teaching Evaluations) created this
        // entry -- its category/criteria are fixed and the frontend disables reassigning them.
        private String createdByModuleCode;
    }

    // ─── Mapping ────────────────────────────────────────────────────────────────────────

    /** The department's own configured Head Title, falling back to its org group's -- null if
     *  neither is set, in which case the frontend shows the org-wide default label instead. */
    private String resolveHeadTitle(com.rit.spms.domain.Department department) {
        if (department == null) return null;
        if (department.getHeadTitle() != null) return department.getHeadTitle();
        return department.getOrgGroup() != null ? department.getOrgGroup().getHeadTitle() : null;
    }

    private AnnualEvaluationResponse mapSummary(AnnualEvaluation e) {
        AnnualEvaluationResponse resp = new AnnualEvaluationResponse();
        resp.setId(e.getId());
        resp.setEmployeeId(e.getEmployee().getId());
        resp.setEmployeeName(e.getEmployee().getFname() + " " + e.getEmployee().getLname());
        resp.setDepartmentName(e.getEmployee().getDepartment() != null ? e.getEmployee().getDepartment().getName() : null);
        resp.setHeadTitle(resolveHeadTitle(e.getEmployee().getDepartment()));
        resp.setHeadId(e.getHead().getId());
        resp.setHeadName(e.getHead().getFname() + " " + e.getHead().getLname());
        resp.setAcademicYearId(e.getAcademicYear().getId());
        resp.setAcademicYearName(e.getAcademicYear().getName());
        resp.setState(e.getState().name());
        resp.setHeadOverallRank(e.getHeadOverallRank());
        resp.setGoalsHeadCommentsStrengths(e.getGoalsHeadCommentsStrengths());
        resp.setGoalsHeadCommentsImprovements(e.getGoalsHeadCommentsImprovements());
        resp.setGoalsEmployeeComments(e.getGoalsEmployeeComments());
        resp.setEmployeeFinalSummary(e.getEmployeeFinalSummary());
        resp.setGoalsEmployeeSelfRank(e.getGoalsEmployeeSelfRank());
        resp.setGoalsHeadRank(e.getGoalsHeadRank());
        resp.setEmployeeSubmittedAt(e.getEmployeeSubmittedAt());
        resp.setReturnedToEmployeeAt(e.getReturnedToEmployeeAt());
        resp.setHeadSubmittedAt(e.getHeadSubmittedAt());
        resp.setHeadSignedAt(e.getHeadSignedAt());
        resp.setHeadSignatureName(e.getHeadSignatureName());
        resp.setEmployeeSignedAt(e.getEmployeeSignedAt());
        resp.setEmployeeSignatureName(e.getEmployeeSignatureName());
        resp.setEmployeeRefused(e.getEmployeeRefused());
        resp.setEmployeeRefusalRationale(e.getEmployeeRefusalRationale());
        resp.setLocked(e.isLocked());
        resp.setConcluded(e.isConcluded());
        resp.setNextCycleNotesStrengths(e.getNextCycleNotesStrengths());
        resp.setNextCycleNotesWeaknesses(e.getNextCycleNotesWeaknesses());
        resp.setNextCycleGenerationRequestedAt(e.getNextCycleGenerationRequestedAt());
        resp.setNextCycleGeneratedAt(e.getNextCycleGeneratedAt());
        resp.setNextCycleGenerationFailureReason(e.getNextCycleGenerationFailureReason());
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
            c.setSortOrder(r.getCategory().getSortOrder());
            c.setEmployeeSelfRank(r.getEmployeeSelfRank());
            c.setHeadCategoryRank(r.getHeadCategoryRank());
            c.setHeadCommentsStrengths(r.getHeadCommentsStrengths());
            c.setHeadCommentsImprovements(r.getHeadCommentsImprovements());
            c.setEmployeeComments(r.getEmployeeComments());
            return c;
        }).toList());
        resp.setCriteriaResults(evaluationService.getCriteriaResults(e.getId()).stream().map(r -> {
            CriteriaResultResponse c = new CriteriaResultResponse();
            c.setCriteriaId(r.getCriteria().getId());
            c.setCriteriaName(r.getCriteria().getCriteriaName());
            c.setCategoryId(r.getCriteria().getCategory().getId());
            c.setHeadRank(r.getHeadRank());
            c.setEmployeeNothingToReport(r.getEmployeeNothingToReport());
            c.setEmployeeComments(r.getEmployeeComments());
            c.setRubricUnsatisfactory(r.getCriteria().getRubricUnsatisfactory());
            c.setRubricMeetsExpectations(r.getCriteria().getRubricMeetsExpectations());
            c.setRubricExceedsExpectations(r.getCriteria().getRubricExceedsExpectations());
            List<CriteriaAchievementModule> moduleAssignments = achievementModuleRepository.findByCriteriaId(r.getCriteria().getId());
            c.setAchievementModuleCodes(moduleAssignments.stream().map(CriteriaAchievementModule::getModuleCode).toList());
            c.setAchievementModuleMaxPerYear(moduleAssignments.stream()
                    .collect(java.util.stream.Collectors.toMap(CriteriaAchievementModule::getModuleCode, CriteriaAchievementModule::getMaxAchievementsPerYear)));
            c.setAchievementModuleMandatory(moduleAssignments.stream()
                    .collect(java.util.stream.Collectors.toMap(CriteriaAchievementModule::getModuleCode, CriteriaAchievementModule::getMandatory)));
            c.setAchievementModuleDisplayNames(moduleAssignments.stream()
                    .filter(m -> m.getDisplayName() != null)
                    .collect(java.util.stream.Collectors.toMap(CriteriaAchievementModule::getModuleCode, CriteriaAchievementModule::getDisplayName)));
            c.setInfoToolAssignments(infoToolAssignmentRepository.findByCriteriaId(r.getCriteria().getId()).stream().map(a -> {
                InfoToolAssignmentSummary summary = new InfoToolAssignmentSummary();
                summary.setToolCode(a.getToolCode());
                summary.setDisplayName(a.getDisplayName());
                summary.setRepositorySourceType(a.getRepositorySourceType());
                return summary;
            }).toList());
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
            var achResp = achievementService.toResponse(entry.getAchievement(), currentUserId);
            r.setAchievementId(achResp.getId());
            r.setAchievementTitle(achResp.getTitle());
            r.setAchievementDetails(achResp.getDetails());
            r.setAchievementTypeId(achResp.getAchievementTypeId());
            r.setAchievementTypeName(achResp.getAchievementTypeName());
            r.setCustomTypeName(achResp.getCustomTypeName());
            r.setPrivateNotes(achResp.getPrivateNotes());
            r.setAuthorId(achResp.getAuthorId());
            r.setAuthorName(achResp.getAuthorName());
            r.setAssessmentPeriodId(achResp.getAssessmentPeriodId());
            r.setAssessmentPeriodName(achResp.getAssessmentPeriodName());
            r.setRecordedAt(achResp.getRecordedAt());
            r.setCanEdit(achResp.isCanEdit());
            r.setCanDelete(achResp.isCanDelete());
            r.setCategoryId(entry.getCategory().getId());
            if (entry.getCriteria() != null) {
                r.setCriteriaId(entry.getCriteria().getId());
            }
            if (entry.getGoal() != null) {
                r.setGoalId(entry.getGoal().getId());
            }
            r.setCreatedByModuleCode(entry.getAchievement().getCreatedByModuleCode());
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

    private NextCycleGoalResponse map(AnnualEvaluationNextCycleGoal g) {
        NextCycleGoalResponse resp = new NextCycleGoalResponse();
        resp.setId(g.getId());
        resp.setCategoryId(g.getCategory().getId());
        resp.setCategoryName(g.getCategory().getCategoryName());
        resp.setSuggestedTitle(g.getSuggestedTitle());
        resp.setSuggestedDescription(g.getSuggestedDescription());
        resp.setRationale(g.getRationale());
        resp.setGeneratedByModel(g.getGeneratedByModel());
        resp.setSortOrder(g.getSortOrder());
        resp.setRubricUnsatisfactory(g.getRubricUnsatisfactory());
        resp.setRubricMeetsExpectations(g.getRubricMeetsExpectations());
        resp.setRubricExceedsExpectations(g.getRubricExceedsExpectations());
        resp.setLeaderActionType(g.getLeaderActionType());
        resp.setLeaderEditedTitle(g.getLeaderEditedTitle());
        resp.setLeaderEditedDescription(g.getLeaderEditedDescription());
        resp.setEmployeeActionType(g.getEmployeeActionType());
        resp.setEmployeeEditedTitle(g.getEmployeeEditedTitle());
        resp.setEmployeeEditedDescription(g.getEmployeeEditedDescription());
        resp.setUsed(Boolean.TRUE.equals(g.getUsed()));
        return resp;
    }
}
