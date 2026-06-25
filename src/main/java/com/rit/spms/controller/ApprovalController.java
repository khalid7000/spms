package com.rit.spms.controller;

import com.rit.spms.dto.response.ApiResponse;
import com.rit.spms.dto.response.ApprovalRequestResponse;
import com.rit.spms.security.UserPrincipal;
import com.rit.spms.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    /** Strategies waiting for the current user's approval. */
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<ApprovalRequestResponse>>> myPending(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                approvalService.getPendingForUser(principal.getId())));
    }

    /** All approval records for a strategy — lets the owner track progress. */
    @GetMapping("/strategy/{strategyId}")
    public ResponseEntity<ApiResponse<List<ApprovalRequestResponse>>> strategyStatus(
            @PathVariable Long strategyId) {
        return ResponseEntity.ok(ApiResponse.success(
                approvalService.getApprovalStatusForStrategy(strategyId)));
    }

    /** Approve a strategy's deployment request. */
    @PostMapping("/strategy/{strategyId}/approve")
    public ResponseEntity<ApiResponse<Void>> approve(
            @PathVariable Long strategyId,
            @AuthenticationPrincipal UserPrincipal principal) {
        approvalService.approve(strategyId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Strategy approved", null));
    }
}
