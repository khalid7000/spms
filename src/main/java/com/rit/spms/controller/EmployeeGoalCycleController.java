package com.rit.spms.controller;

import com.rit.spms.domain.AppUser;
import com.rit.spms.domain.EmployeeGoal;
import com.rit.spms.domain.EmployeeGoalCycle;
import com.rit.spms.domain.EmployeeGoalSuggestion;
import com.rit.spms.domain.PortfolioCategory;
import com.rit.spms.domain.enums.PortfolioReviewActionType;
import com.rit.spms.dto.response.ApiResponse;
import com.rit.spms.dto.response.UserResponse;
import com.rit.spms.security.UserPrincipal;
import com.rit.spms.service.EmployeeGoalCycleService;
import com.rit.spms.service.EmployeeGoalSuggestionService;
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

@RestController
@RequestMapping("/api/portfolio/cycles")
@RequiredArgsConstructor
public class EmployeeGoalCycleController {

    private final EmployeeGoalCycleService cycleService;
    private final EmployeeGoalSuggestionService suggestionService;

    @GetMapping("/my-direct-reports")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getDirectReports(@AuthenticationPrincipal UserPrincipal principal) {
        List<UserResponse> reports = cycleService.getDirectReports(principal.getId())
                .stream().map(UserResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.success(reports));
    }

    // ─── Cycle lifecycle ────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CycleResponse>> createOrGetCycle(
            @Valid @RequestBody CreateCycleRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        EmployeeGoalCycle cycle = cycleService.createOrGetCycle(req.getEmployeeId(), req.getAcademicYearId(), principal.getId());
        return ResponseEntity.status(201).body(ApiResponse.success(map(cycle, principal.getId())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CycleResponse>> getCycle(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(map(cycleService.getCycle(id, principal.getId()), principal.getId())));
    }

    @GetMapping("/my-academic-year/{academicYearId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CycleResponse>>> getMyCycles(
            @PathVariable Long academicYearId, @AuthenticationPrincipal UserPrincipal principal) {
        List<CycleResponse> cycles = cycleService.getMyCycles(principal.getId()).stream()
                .filter(c -> c.getAcademicYear().getId().equals(academicYearId))
                .map(c -> map(c, principal.getId())).toList();
        return ResponseEntity.ok(ApiResponse.success(cycles));
    }

    @GetMapping("/team/{academicYearId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CycleResponse>>> getTeamCycles(
            @PathVariable Long academicYearId, @AuthenticationPrincipal UserPrincipal principal) {
        List<CycleResponse> cycles = cycleService.getTeamCycles(principal.getId(), academicYearId)
                .stream().map(c -> map(c, principal.getId())).toList();
        return ResponseEntity.ok(ApiResponse.success(cycles));
    }

    @PutMapping("/{id}/notes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CycleResponse>> updateNotes(
            @PathVariable Long id, @RequestBody UpdateNotesRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        EmployeeGoalCycle cycle = cycleService.updateNotes(id, req.getLeaderStrengths(), req.getLeaderWeaknesses(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success(map(cycle, principal.getId())));
    }

    // ─── Leader stage: AI suggestions ───────────────────────────────────────

    // Fire-and-forget: generateSuggestionsAsync() is @Async and returns immediately, well before
    // the model call finishes, so there's nothing to hand back yet -- 202 signals "started" rather
    // than "done". GoalSettingPage polls GET /{id} and picks up suggestionsGeneratedAt (or
    // generationFailureReason) once the background call lands. recordGenerationRequested() and the
    // async kickoff are called directly here (not from within another @Transactional service
    // method) so the "requested" write commits on its own before the async generator can possibly
    // read/overwrite it -- see the comment on EmployeeGoalCycleService.assertCanGenerateSuggestions.
    @PostMapping("/{id}/generate-suggestions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CycleResponse>> generateSuggestions(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        List<PortfolioCategory> categories = cycleService.assertCanGenerateSuggestions(id, principal.getId());
        suggestionService.recordGenerationRequested(id);
        suggestionService.generateSuggestionsAsync(id, categories);
        return ResponseEntity.status(202).body(ApiResponse.success(
                "AI suggestion generation started", map(cycleService.getCycle(id, principal.getId()), principal.getId())));
    }

    @GetMapping("/{id}/suggestions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<SuggestionResponse>>> getSuggestions(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        List<SuggestionResponse> suggestions = cycleService.getSuggestions(id, principal.getId())
                .stream().map(this::map).toList();
        return ResponseEntity.ok(ApiResponse.success(suggestions));
    }

    @PutMapping("/{id}/suggestions/{suggestionId}/review")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SuggestionResponse>> reviewSuggestion(
            @PathVariable Long id, @PathVariable Long suggestionId,
            @Valid @RequestBody ReviewRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        EmployeeGoalSuggestion suggestion = cycleService.reviewSuggestion(
                id, suggestionId, req.getActionType(), req.getEditedTitle(), req.getEditedDescription(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success(map(suggestion)));
    }

    @PostMapping("/{id}/suggestions/add")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SuggestionResponse>> addSuggestion(
            @PathVariable Long id, @Valid @RequestBody AddSuggestionRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        EmployeeGoalSuggestion suggestion = cycleService.addSuggestion(
                id, req.getCategoryId(), req.getTitle(), req.getDescription(),
                req.getRubricUnsatisfactory(), req.getRubricMeetsExpectations(), req.getRubricExceedsExpectations(),
                principal.getId());
        return ResponseEntity.status(201).body(ApiResponse.success(map(suggestion)));
    }

    @PutMapping("/{id}/suggestions/{suggestionId}/rubric")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SuggestionResponse>> updateSuggestionRubric(
            @PathVariable Long id, @PathVariable Long suggestionId,
            @RequestBody UpdateSuggestionRubricRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        EmployeeGoalSuggestion suggestion = cycleService.updateSuggestionRubric(id, suggestionId,
                req.getRubricUnsatisfactory(), req.getRubricMeetsExpectations(), req.getRubricExceedsExpectations(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success(map(suggestion)));
    }

    @DeleteMapping("/{id}/suggestions/{suggestionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteSuggestion(
            @PathVariable Long id, @PathVariable Long suggestionId, @AuthenticationPrincipal UserPrincipal principal) {
        cycleService.deleteSuggestion(id, suggestionId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Goal removed", null));
    }

    @PostMapping("/{id}/submit-for-review")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CycleResponse>> submitForReview(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Submitted for employee review", map(cycleService.submitForReview(id, principal.getId()), principal.getId())));
    }

    @PostMapping("/{id}/resubmit-for-review")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CycleResponse>> resubmitForReview(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Resubmitted for employee review", map(cycleService.resubmitForReview(id, principal.getId()), principal.getId())));
    }

    // ─── Leader stage: direct goal edits (only while EMPLOYEE_SUBMITTED) ────

    @GetMapping("/{id}/goals")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getCycleGoals(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        List<GoalResponse> goals = cycleService.getCycleGoals(id, principal.getId()).stream().map(this::map).toList();
        return ResponseEntity.ok(ApiResponse.success(goals));
    }

    @PostMapping("/{id}/goals")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<GoalResponse>> addGoal(
            @PathVariable Long id, @Valid @RequestBody AddGoalRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        EmployeeGoal goal = cycleService.addGoal(id, req.getCategoryId(), req.getGoalTitle(), req.getDescription(),
                req.getRubricUnsatisfactory(), req.getRubricMeetsExpectations(), req.getRubricExceedsExpectations(),
                principal.getId());
        return ResponseEntity.status(201).body(ApiResponse.success(map(goal)));
    }

    @PutMapping("/{id}/goals/{goalId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<GoalResponse>> updateGoal(
            @PathVariable Long id, @PathVariable Long goalId,
            @Valid @RequestBody UpdateGoalRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        EmployeeGoal goal = cycleService.updateGoal(id, goalId, req.getGoalTitle(), req.getDescription(),
                req.getCategoryId(), req.getMeasurementId(),
                req.getRubricUnsatisfactory(), req.getRubricMeetsExpectations(), req.getRubricExceedsExpectations(),
                principal.getId());
        return ResponseEntity.ok(ApiResponse.success(map(goal)));
    }

    @DeleteMapping("/{id}/goals/{goalId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(
            @PathVariable Long id, @PathVariable Long goalId, @AuthenticationPrincipal UserPrincipal principal) {
        cycleService.deleteGoal(id, goalId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Goal removed", null));
    }

    // ─── Employee stage ─────────────────────────────────────────────────────

    @PutMapping("/{id}/start-employee-review")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CycleResponse>> startEmployeeReview(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(map(cycleService.startEmployeeReview(id, principal.getId()), principal.getId())));
    }

    @PutMapping("/{id}/goals/{goalId}/review")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<GoalResponse>> reviewGoal(
            @PathVariable Long id, @PathVariable Long goalId,
            @Valid @RequestBody ReviewRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        EmployeeGoal goal = cycleService.reviewGoal(id, goalId, req.getActionType(),
                req.getEditedTitle(), req.getEditedDescription(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success(map(goal)));
    }

    @PostMapping("/{id}/accept")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CycleResponse>> accept(
            @PathVariable Long id, @Valid @RequestBody AcceptCycleRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Goals accepted, signed, and deployed",
                map(cycleService.acceptCycle(id, req.getSignatureName(), principal.getId()), principal.getId())));
    }

    @PostMapping("/{id}/submit-back")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CycleResponse>> submitBack(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Sent back to your department head for more consideration", map(cycleService.submitBack(id, principal.getId()), principal.getId())));
    }

    // ─── DTOs ────────────────────────────────────────────────────────────────

    @lombok.Data
    public static class CreateCycleRequest {
        @NotNull private Long employeeId;
        @NotNull private Long academicYearId;
    }

    @lombok.Data
    public static class UpdateNotesRequest {
        private String leaderStrengths;
        private String leaderWeaknesses;
    }

    @lombok.Data
    public static class ReviewRequest {
        @NotNull private PortfolioReviewActionType actionType;
        private String editedTitle;
        private String editedDescription;
    }

    @lombok.Data
    public static class AddSuggestionRequest {
        @NotNull private Long categoryId;
        private String title;
        private String description;
        private String rubricUnsatisfactory;
        private String rubricMeetsExpectations;
        private String rubricExceedsExpectations;
    }

    @lombok.Data
    public static class UpdateSuggestionRubricRequest {
        private String rubricUnsatisfactory;
        private String rubricMeetsExpectations;
        private String rubricExceedsExpectations;
    }

    @lombok.Data
    public static class AddGoalRequest {
        @NotNull private Long categoryId;
        private String goalTitle;
        private String description;
        private String rubricUnsatisfactory;
        private String rubricMeetsExpectations;
        private String rubricExceedsExpectations;
    }

    @lombok.Data
    public static class UpdateGoalRequest {
        private String goalTitle;
        private String description;
        private Long categoryId;
        private Long measurementId;
        private String rubricUnsatisfactory;
        private String rubricMeetsExpectations;
        private String rubricExceedsExpectations;
    }

    @lombok.Data
    public static class AcceptCycleRequest {
        @NotBlank(message = "Type your name to sign before accepting these goals")
        private String signatureName;
    }

    @lombok.Data
    public static class CycleResponse {
        private Long id;
        private Long employeeId;
        private String employeeName;
        private Long leaderId;
        private String leaderName;
        private Long academicYearId;
        private String state;
        private String leaderStrengths;
        private String leaderWeaknesses;
        private LocalDateTime leaderSubmittedAt;
        private LocalDateTime employeeAcceptedAt;
        private String employeeSignatureName;
        private LocalDateTime generationRequestedAt;
        private LocalDateTime suggestionsGeneratedAt;
        private String generationFailureReason;
    }

    @lombok.Data
    public static class SuggestionResponse {
        private Long id;
        private Long categoryId;
        private String categoryName;
        private String suggestedTitle;
        private String suggestedDescription;
        private String rationale;
        private String generatedByModel;
        private Integer sortOrder;
        private PortfolioReviewActionType leaderActionType;
        private String editedTitle;
        private String editedDescription;
        private String rubricUnsatisfactory;
        private String rubricMeetsExpectations;
        private String rubricExceedsExpectations;
    }

    @lombok.Data
    public static class GoalResponse {
        private Long id;
        private Long cycleId;
        private Long categoryId;
        private String categoryName;
        private Long measurementId;
        private String goalTitle;
        private String description;
        private Integer sortOrder;
        private PortfolioReviewActionType employeeActionType;
        private String employeeEditedTitle;
        private String employeeEditedDescription;
        private String rubricUnsatisfactory;
        private String rubricMeetsExpectations;
        private String rubricExceedsExpectations;
    }

    // ─── Mappers ─────────────────────────────────────────────────────────────

    /**
     * leaderStrengths/leaderWeaknesses are the leader's private notes about the employee -- never
     * included unless the viewer IS that leader (or an admin), regardless of which endpoint this
     * is called from (including ones the employee themselves calls, e.g. accepting the cycle).
     */
    private CycleResponse map(EmployeeGoalCycle cycle, Long viewingUserId) {
        CycleResponse resp = new CycleResponse();
        resp.setId(cycle.getId());
        resp.setEmployeeId(cycle.getEmployee().getId());
        resp.setEmployeeName(cycle.getEmployee().getFname() + " " + cycle.getEmployee().getLname());
        resp.setLeaderId(cycle.getLeader().getId());
        resp.setLeaderName(cycle.getLeader().getFname() + " " + cycle.getLeader().getLname());
        resp.setAcademicYearId(cycle.getAcademicYear().getId());
        resp.setState(cycle.getState().toString());
        if (cycleService.isLeaderOrAdmin(cycle, viewingUserId)) {
            resp.setLeaderStrengths(cycle.getLeaderStrengths());
            resp.setLeaderWeaknesses(cycle.getLeaderWeaknesses());
        }
        resp.setLeaderSubmittedAt(cycle.getLeaderSubmittedAt());
        resp.setEmployeeAcceptedAt(cycle.getEmployeeAcceptedAt());
        resp.setEmployeeSignatureName(cycle.getEmployeeSignatureName());
        resp.setGenerationRequestedAt(cycle.getGenerationRequestedAt());
        resp.setSuggestionsGeneratedAt(cycle.getSuggestionsGeneratedAt());
        resp.setGenerationFailureReason(cycle.getGenerationFailureReason());
        return resp;
    }

    private SuggestionResponse map(EmployeeGoalSuggestion s) {
        SuggestionResponse resp = new SuggestionResponse();
        resp.setId(s.getId());
        resp.setCategoryId(s.getCategory().getId());
        resp.setCategoryName(s.getCategory().getCategoryName());
        resp.setSuggestedTitle(s.getSuggestedTitle());
        resp.setSuggestedDescription(s.getSuggestedDescription());
        resp.setRationale(s.getRationale());
        resp.setGeneratedByModel(s.getGeneratedByModel());
        resp.setSortOrder(s.getSortOrder());
        resp.setLeaderActionType(s.getLeaderActionType());
        resp.setEditedTitle(s.getEditedTitle());
        resp.setEditedDescription(s.getEditedDescription());
        resp.setRubricUnsatisfactory(s.getRubricUnsatisfactory());
        resp.setRubricMeetsExpectations(s.getRubricMeetsExpectations());
        resp.setRubricExceedsExpectations(s.getRubricExceedsExpectations());
        return resp;
    }

    private GoalResponse map(EmployeeGoal g) {
        GoalResponse resp = new GoalResponse();
        resp.setId(g.getId());
        resp.setCycleId(g.getCycle().getId());
        resp.setCategoryId(g.getCategory().getId());
        resp.setCategoryName(g.getCategory().getCategoryName());
        resp.setMeasurementId(g.getMeasurement() != null ? g.getMeasurement().getId() : null);
        resp.setGoalTitle(g.getGoalTitle());
        resp.setDescription(g.getDescription());
        resp.setSortOrder(g.getSortOrder());
        resp.setEmployeeActionType(g.getEmployeeActionType());
        resp.setEmployeeEditedTitle(g.getEmployeeEditedTitle());
        resp.setEmployeeEditedDescription(g.getEmployeeEditedDescription());
        resp.setRubricUnsatisfactory(g.getRubricUnsatisfactory());
        resp.setRubricMeetsExpectations(g.getRubricMeetsExpectations());
        resp.setRubricExceedsExpectations(g.getRubricExceedsExpectations());
        return resp;
    }
}
