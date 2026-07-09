package com.rit.spms.controller;

import com.rit.spms.domain.enums.SwotReviewTargetType;
import com.rit.spms.dto.request.SwotAlternativeProposalRequest;
import com.rit.spms.dto.request.SwotFinalDecisionsRequest;
import com.rit.spms.dto.request.SwotGoalAdditionRequest;
import com.rit.spms.dto.request.SwotReviewItemRequest;
import com.rit.spms.dto.response.ApiResponse;
import com.rit.spms.dto.response.SwotAlternativeProposalResponse;
import com.rit.spms.dto.response.SwotFinalizationResultResponse;
import com.rit.spms.dto.response.SwotReviewItemResponse;
import com.rit.spms.dto.response.SwotReviewSummaryResponse;
import com.rit.spms.dto.response.SwotSuggestedGoalAdditionResponse;
import com.rit.spms.dto.response.SwotSuggestionResponse;
import com.rit.spms.security.UserPrincipal;
import com.rit.spms.service.PermissionService;
import com.rit.spms.service.SwotFinalizationService;
import com.rit.spms.service.SwotReviewService;
import com.rit.spms.service.SwotSuggestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** AI suggestion generation, per-user review, and owner finalization endpoints for the SWOT workflow. */
@RestController
@RequestMapping("/api/strategies/{strategyId}/swot")
@RequiredArgsConstructor
public class SwotSuggestionController {

    private final SwotSuggestionService swotSuggestionService;
    private final SwotReviewService swotReviewService;
    private final SwotFinalizationService swotFinalizationService;
    private final PermissionService permissionService;

    // Fire-and-forget: generateSuggestions() is @Async and returns immediately, well before the
    // model call finishes, so there's nothing to hand back yet — 202 signals "started" rather than
    // "done". The frontend polls GET /status and picks up the REVIEWING transition once it lands.
    // recordGenerationRequested() runs first and commits synchronously, so that same poll can show
    // "submitted at X, Y elapsed" the whole time generation is running, not just after it finishes.
    @PostMapping("/suggestions/generate")
    public ResponseEntity<ApiResponse<Void>> generateSuggestions(
            @PathVariable Long strategyId, @AuthenticationPrincipal UserPrincipal principal) {
        permissionService.assertCanGenerateSuggestions(principal.getId(), strategyId);
        swotSuggestionService.recordGenerationRequested(strategyId);
        swotSuggestionService.generateSuggestions(strategyId);
        return ResponseEntity.status(202).body(ApiResponse.success("AI suggestion generation started", null));
    }

    @GetMapping("/suggestions")
    public ResponseEntity<ApiResponse<List<SwotSuggestionResponse>>> getSuggestions(
            @PathVariable Long strategyId, @AuthenticationPrincipal UserPrincipal principal) {
        permissionService.assertCanRead(principal.getId(), strategyId);
        return ResponseEntity.ok(ApiResponse.success(swotSuggestionService.getSuggestions(strategyId)));
    }

    @PostMapping("/suggestions/{targetType}/{targetId}/review")
    public ResponseEntity<ApiResponse<SwotReviewItemResponse>> submitReviewItem(
            @PathVariable Long strategyId, @PathVariable SwotReviewTargetType targetType, @PathVariable Long targetId,
            @Valid @RequestBody SwotReviewItemRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        permissionService.assertCanReview(principal.getId(), strategyId);
        SwotReviewItemResponse response = swotReviewService.submitReviewItem(strategyId, principal.getId(),
                targetType, targetId, req.getActionType(), req.getEditedTitle(), req.getEditedDescription());
        return ResponseEntity.ok(ApiResponse.success("Review recorded", response));
    }

    @GetMapping("/review/mine")
    public ResponseEntity<ApiResponse<List<SwotReviewItemResponse>>> getMyReviewItems(
            @PathVariable Long strategyId, @AuthenticationPrincipal UserPrincipal principal) {
        permissionService.assertCanRead(principal.getId(), strategyId);
        return ResponseEntity.ok(ApiResponse.success(swotReviewService.getMyReviewItems(strategyId, principal.getId())));
    }

    @PostMapping("/alternatives")
    public ResponseEntity<ApiResponse<SwotAlternativeProposalResponse>> proposeAlternative(
            @PathVariable Long strategyId, @Valid @RequestBody SwotAlternativeProposalRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        permissionService.assertCanReview(principal.getId(), strategyId);
        SwotAlternativeProposalResponse response = swotReviewService.submitAlternativeProposal(
                strategyId, principal.getId(), req.getName(), req.getRationale(), req.getGoals());
        return ResponseEntity.status(201).body(ApiResponse.success("Alternative proposed", response));
    }

    @GetMapping("/alternatives")
    public ResponseEntity<ApiResponse<List<SwotAlternativeProposalResponse>>> getAlternatives(
            @PathVariable Long strategyId, @AuthenticationPrincipal UserPrincipal principal) {
        permissionService.assertCanRead(principal.getId(), strategyId);
        boolean allForOwner = permissionService.isOwner(principal.getId(), strategyId);
        return ResponseEntity.ok(ApiResponse.success(
                swotReviewService.getMyAlternatives(strategyId, principal.getId(), allForOwner)));
    }

    @DeleteMapping("/alternatives/{proposalId}")
    public ResponseEntity<ApiResponse<Void>> deleteAlternative(
            @PathVariable Long strategyId, @PathVariable Long proposalId,
            @AuthenticationPrincipal UserPrincipal principal) {
        permissionService.assertCanReview(principal.getId(), strategyId);
        swotReviewService.deleteAlternativeProposal(strategyId, principal.getId(), proposalId);
        return ResponseEntity.ok(ApiResponse.success("Alternative removed", null));
    }

    // A brand-new goal under an existing AI-suggested area — supplements, doesn't replace, the AI's
    // own goals. Available to an Editor still reviewing (REVIEWING) or the Owner at finalization
    // (FINALIZING); assertCanProposeGoalAddition picks the right check based on role/phase.
    @PostMapping("/suggestions/{areaId}/goal-additions")
    public ResponseEntity<ApiResponse<SwotSuggestedGoalAdditionResponse>> proposeGoalAddition(
            @PathVariable Long strategyId, @PathVariable Long areaId,
            @Valid @RequestBody SwotGoalAdditionRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        permissionService.assertCanProposeGoalAddition(principal.getId(), strategyId);
        SwotSuggestedGoalAdditionResponse response = swotReviewService.proposeGoalAddition(
                strategyId, principal.getId(), areaId, req.getTitle(), req.getDescription());
        return ResponseEntity.status(201).body(ApiResponse.success("Goal proposed", response));
    }

    // Non-owner callers see only their own proposed additions; the Owner sees every proposal
    // (mirrors getAlternatives above).
    @GetMapping("/goal-additions")
    public ResponseEntity<ApiResponse<List<SwotSuggestedGoalAdditionResponse>>> getGoalAdditions(
            @PathVariable Long strategyId, @AuthenticationPrincipal UserPrincipal principal) {
        permissionService.assertCanRead(principal.getId(), strategyId);
        boolean allForOwner = permissionService.isOwner(principal.getId(), strategyId);
        return ResponseEntity.ok(ApiResponse.success(
                swotReviewService.getGoalAdditions(strategyId, principal.getId(), allForOwner)));
    }

    @DeleteMapping("/goal-additions/{additionId}")
    public ResponseEntity<ApiResponse<Void>> deleteGoalAddition(
            @PathVariable Long strategyId, @PathVariable Long additionId,
            @AuthenticationPrincipal UserPrincipal principal) {
        permissionService.assertCanProposeGoalAddition(principal.getId(), strategyId);
        swotReviewService.deleteGoalAddition(strategyId, principal.getId(), additionId);
        return ResponseEntity.ok(ApiResponse.success("Goal removed", null));
    }

    @PostMapping("/review/submit")
    public ResponseEntity<ApiResponse<Void>> submitFullReview(
            @PathVariable Long strategyId, @AuthenticationPrincipal UserPrincipal principal) {
        permissionService.assertCanReview(principal.getId(), strategyId);
        swotReviewService.submitFullReview(strategyId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Review submitted", null));
    }

    @GetMapping("/finalization/summary")
    public ResponseEntity<ApiResponse<SwotReviewSummaryResponse>> getFinalizationSummary(
            @PathVariable Long strategyId, @AuthenticationPrincipal UserPrincipal principal) {
        permissionService.assertCanFinalize(principal.getId(), strategyId);
        return ResponseEntity.ok(ApiResponse.success(swotReviewService.getReviewSummary(strategyId)));
    }

    @PostMapping("/finalization/decisions")
    public ResponseEntity<ApiResponse<Void>> saveDraftDecisions(
            @PathVariable Long strategyId, @Valid @RequestBody SwotFinalDecisionsRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        permissionService.assertCanFinalize(principal.getId(), strategyId);
        swotFinalizationService.saveDraftDecisions(strategyId, principal.getId(), req.getDecisions());
        return ResponseEntity.ok(ApiResponse.success("Draft decisions saved", null));
    }

    @PostMapping("/finalization/submit")
    public ResponseEntity<ApiResponse<SwotFinalizationResultResponse>> finalizeSwot(
            @PathVariable Long strategyId, @AuthenticationPrincipal UserPrincipal principal) {
        permissionService.assertCanFinalize(principal.getId(), strategyId);
        SwotFinalizationResultResponse result = swotFinalizationService.finalize(strategyId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Draft strategy created", result));
    }
}
