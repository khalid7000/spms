package com.rit.spms.controller;

import com.rit.spms.domain.enums.SwotQuadrant;
import com.rit.spms.dto.request.SwotSynonymRequest;
import com.rit.spms.dto.request.SwotVoteRequest;
import com.rit.spms.dto.request.SwotWordRequest;
import com.rit.spms.dto.response.ApiResponse;
import com.rit.spms.dto.response.SwotEntryResponse;
import com.rit.spms.dto.response.SwotResultResponse;
import com.rit.spms.dto.response.SwotStatusResponse;
import com.rit.spms.dto.response.SwotVisualizationWordResponse;
import com.rit.spms.dto.response.SwotVoteBallotResponse;
import com.rit.spms.security.UserPrincipal;
import com.rit.spms.service.PermissionService;
import com.rit.spms.service.SwotService;
import com.rit.spms.service.SwotVotingService;
import com.rit.spms.service.SynonymProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Word-collection, ranked voting, and results endpoints for the SWOT workflow. */
@RestController
@RequestMapping("/api/strategies/{strategyId}/swot")
@RequiredArgsConstructor
public class SwotController {

    private final SwotService swotService;
    private final SwotVotingService swotVotingService;
    private final SynonymProvider synonymProvider;
    private final PermissionService permissionService;

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<SwotStatusResponse>> getStatus(
            @PathVariable Long strategyId, @AuthenticationPrincipal UserPrincipal principal) {
        permissionService.assertCanRead(principal.getId(), strategyId);
        return ResponseEntity.ok(ApiResponse.success(swotService.getStatus(strategyId, principal.getId())));
    }

    @PostMapping("/start")
    public ResponseEntity<ApiResponse<SwotStatusResponse>> start(
            @PathVariable Long strategyId, @AuthenticationPrincipal UserPrincipal principal) {
        permissionService.assertCanStartSwot(principal.getId(), strategyId);
        swotService.startSwot(strategyId, principal.getId());
        return ResponseEntity.status(201).body(ApiResponse.success("SWOT analysis started",
                swotService.getStatus(strategyId, principal.getId())));
    }

    @PostMapping("/entries")
    public ResponseEntity<ApiResponse<SwotEntryResponse>> submitWord(
            @PathVariable Long strategyId, @Valid @RequestBody SwotWordRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        permissionService.assertCanSubmitSwotEntry(principal.getId(), strategyId);
        SwotEntryResponse response = swotService.submitWord(
                strategyId, principal.getId(), req.getQuadrant(), req.getWord(), req.getJustification());
        return ResponseEntity.status(201).body(ApiResponse.success("Word added", response));
    }

    @GetMapping("/entries")
    public ResponseEntity<ApiResponse<List<SwotEntryResponse>>> getMyEntries(
            @PathVariable Long strategyId, @RequestParam(required = false) SwotQuadrant quadrant,
            @AuthenticationPrincipal UserPrincipal principal) {
        permissionService.assertCanRead(principal.getId(), strategyId);
        return ResponseEntity.ok(ApiResponse.success(swotService.getMyEntries(strategyId, principal.getId(), quadrant)));
    }

    @DeleteMapping("/entries/{entryId}")
    public ResponseEntity<ApiResponse<Void>> deleteWord(
            @PathVariable Long strategyId, @PathVariable Long entryId,
            @AuthenticationPrincipal UserPrincipal principal) {
        permissionService.assertCanSubmitSwotEntry(principal.getId(), strategyId);
        swotService.deleteWord(strategyId, principal.getId(), entryId);
        return ResponseEntity.ok(ApiResponse.success("Word removed", null));
    }

    @PostMapping("/synonyms")
    public ResponseEntity<ApiResponse<List<String>>> suggestSynonyms(
            @PathVariable Long strategyId, @Valid @RequestBody SwotSynonymRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        permissionService.assertCanSubmitSwotEntry(principal.getId(), strategyId);
        return ResponseEntity.ok(ApiResponse.success(
                synonymProvider.suggestSynonyms(req.getQuadrant(), req.getPartialWord())));
    }

    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<SwotStatusResponse>> submitFullSwot(
            @PathVariable Long strategyId, @AuthenticationPrincipal UserPrincipal principal) {
        permissionService.assertCanSubmitSwotEntry(principal.getId(), strategyId);
        return ResponseEntity.ok(ApiResponse.success("SWOT analysis submitted",
                swotService.submitFullSwot(strategyId, principal.getId())));
    }

    @GetMapping("/visualization")
    public ResponseEntity<ApiResponse<List<SwotVisualizationWordResponse>>> getVisualization(
            @PathVariable Long strategyId, @RequestParam(required = false) SwotQuadrant quadrant,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (!permissionService.canViewOwnVisualization(principal.getId(), strategyId)) {
            permissionService.assertOwner(principal.getId(), strategyId);
        }
        return ResponseEntity.ok(ApiResponse.success(swotService.getVisualization(strategyId, quadrant)));
    }

    @GetMapping("/vote/ballot")
    public ResponseEntity<ApiResponse<SwotVoteBallotResponse>> getBallot(
            @PathVariable Long strategyId, @RequestParam SwotQuadrant quadrant,
            @AuthenticationPrincipal UserPrincipal principal) {
        permissionService.assertCanRead(principal.getId(), strategyId);
        return ResponseEntity.ok(ApiResponse.success(swotVotingService.getBallot(strategyId, quadrant)));
    }

    @PostMapping("/vote")
    public ResponseEntity<ApiResponse<Void>> submitVotes(
            @PathVariable Long strategyId, @Valid @RequestBody SwotVoteRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        permissionService.assertCanVote(principal.getId(), strategyId);
        swotVotingService.submitVotes(strategyId, principal.getId(), req.getRankedWordsByQuadrant());
        return ResponseEntity.ok(ApiResponse.success("Vote submitted", null));
    }

    @GetMapping("/results")
    public ResponseEntity<ApiResponse<List<SwotResultResponse>>> getResults(
            @PathVariable Long strategyId, @AuthenticationPrincipal UserPrincipal principal) {
        permissionService.assertCanViewResults(principal.getId(), strategyId);
        return ResponseEntity.ok(ApiResponse.success(swotVotingService.getResults(strategyId)));
    }
}
