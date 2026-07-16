package com.rit.spms.controller;

import com.rit.spms.domain.PortfolioEntry;
import com.rit.spms.domain.TeachingEvaluationSession;
import com.rit.spms.dto.response.ApiResponse;
import com.rit.spms.security.UserPrincipal;
import com.rit.spms.service.TeachingEvaluationSessionService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * The "Teaching Evaluations" achievement module's recording flow -- see
 * {@link TeachingEvaluationSessionService}. Employee-only (an evaluation's own DRAFT edit window).
 */
@RestController
@RequestMapping("/api/portfolio/evaluations/{evaluationId}/teaching-evaluation-sessions")
@RequiredArgsConstructor
public class TeachingEvaluationSessionController {

    private final TeachingEvaluationSessionService sessionService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SessionResponse>> getOrCreateSession(
            @PathVariable Long evaluationId, @RequestParam Long criteriaId,
            @AuthenticationPrincipal UserPrincipal principal) {
        TeachingEvaluationSession session = sessionService.getOrCreateSession(evaluationId, criteriaId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(map(session)));
    }

    @PutMapping("/{sessionId}/notes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SessionResponse>> updateNote(
            @PathVariable Long evaluationId, @PathVariable Long sessionId,
            @RequestBody UpdateNoteRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        TeachingEvaluationSession session = sessionService.updateNote(sessionId, req.getLocalFolderNote(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success(map(session)));
    }

    @PostMapping("/{sessionId}/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SessionResponse>> uploadFiles(
            @PathVariable Long evaluationId, @PathVariable Long sessionId,
            @RequestParam("files") List<MultipartFile> files, @AuthenticationPrincipal UserPrincipal principal) {
        TeachingEvaluationSession session = sessionService.uploadFiles(sessionId, files, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Files processed", map(session)));
    }

    // Fire-and-forget, same pattern as every other AI-generation endpoint in this codebase:
    // recordGenerationRequested() and the async kickoff are called directly here (not from within
    // another @Transactional service method) so the "requested" write commits on its own before
    // the async generator can possibly read/overwrite it. See
    // EmployeeGoalCycleController.generateSuggestions for the fullest explanation of why.
    @PostMapping("/{sessionId}/generate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SessionResponse>> generateDraft(
            @PathVariable Long evaluationId, @PathVariable Long sessionId, @AuthenticationPrincipal UserPrincipal principal) {
        TeachingEvaluationSession session = sessionService.assertCanGenerateDraft(sessionId, principal.getId());
        sessionService.recordGenerationRequested(sessionId);
        sessionService.generateDraftAsync(sessionId);
        return ResponseEntity.status(202).body(ApiResponse.success("AI draft generation started", map(session)));
    }

    @PostMapping("/{sessionId}/finalize")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Long>> finalizeAchievement(
            @PathVariable Long evaluationId, @PathVariable Long sessionId,
            @jakarta.validation.Valid @RequestBody FinalizeRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        PortfolioEntry entry = sessionService.finalizeAchievement(sessionId, req.getTitle(), req.getAchievementTypeId(),
                req.getCustomTypeName(), req.getReflection(), req.getPrivateNotes(), req.getGoalId(), req.getCategoryRating(),
                req.getEvidenceUrl(), principal.getId());
        return ResponseEntity.status(201).body(ApiResponse.success("Achievement recorded", entry.getId()));
    }

    @DeleteMapping("/{sessionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteSession(
            @PathVariable Long evaluationId, @PathVariable Long sessionId, @AuthenticationPrincipal UserPrincipal principal) {
        sessionService.deleteSession(sessionId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Session cleared", null));
    }

    // ─── DTOs ────────────────────────────────────────────────────────────────

    @lombok.Data
    public static class UpdateNoteRequest {
        private String localFolderNote;
    }

    @lombok.Data
    public static class FinalizeRequest {
        @NotBlank private String title;
        @NotNull private Long achievementTypeId;
        private String customTypeName;
        // The employee's own required reflection on the evaluations and the AI-generated review --
        // the AI review itself is never accepted from the client, see finalizeAchievement.
        @NotBlank(message = "Add your reflection before saving this achievement") private String reflection;
        private String privateNotes;
        private Long goalId;
        private Integer categoryRating;
        @NotBlank(message = "Evidence/Link is required") private String evidenceUrl;
    }

    @lombok.Data
    public static class SessionResponse {
        private Long id;
        private Long evaluationId;
        private Long criteriaId;
        private String localFolderNote;
        private String uploadedFileNames;
        private LocalDateTime generationRequestedAt;
        private LocalDateTime generatedAt;
        private String generationFailureReason;
        private String draftDetails;
    }

    private SessionResponse map(TeachingEvaluationSession s) {
        SessionResponse resp = new SessionResponse();
        resp.setId(s.getId());
        resp.setEvaluationId(s.getEvaluation().getId());
        resp.setCriteriaId(s.getCriteria().getId());
        resp.setLocalFolderNote(s.getLocalFolderNote());
        resp.setUploadedFileNames(s.getUploadedFileNames());
        resp.setGenerationRequestedAt(s.getGenerationRequestedAt());
        resp.setGeneratedAt(s.getGeneratedAt());
        resp.setGenerationFailureReason(s.getGenerationFailureReason());
        resp.setDraftDetails(s.getDraftDetails());
        return resp;
    }
}
