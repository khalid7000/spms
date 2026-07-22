package com.rit.spms.controller;

import com.rit.spms.dto.request.CreateApprovalDelegationRequest;
import com.rit.spms.dto.response.ApiResponse;
import com.rit.spms.dto.response.ApprovalDelegationResponse;
import com.rit.spms.security.UserPrincipal;
import com.rit.spms.service.ApprovalDelegationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** The Approval Delegation Console's backend: any employee holding a headship-derived approval
 *  authority (Strategy approval chains, VSM author-grant approval, ...) can hand it to another
 *  employee for a bounded window, subject to the eligibility rules in {@link
 *  com.rit.spms.service.ApprovalDelegationService}. */
@RestController
@RequestMapping("/api/approval-delegations")
@RequiredArgsConstructor
public class ApprovalDelegationController {

    private final ApprovalDelegationService approvalDelegationService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ApprovalDelegationResponse>> create(
            @Valid @RequestBody CreateApprovalDelegationRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        var delegation = approvalDelegationService.createDelegation(req, principal.getId());
        return ResponseEntity.status(201).body(ApiResponse.success(
                delegation.getStatus() == com.rit.spms.domain.enums.ApprovalDelegationStatus.PENDING_MANAGER_APPROVAL
                        ? "Delegation created, awaiting your manager's approval" : "Delegation is now active",
                ApprovalDelegationResponse.from(delegation)));
    }

    @GetMapping("/mine")
    public ResponseEntity<ApiResponse<List<ApprovalDelegationResponse>>> getMine(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                approvalDelegationService.getMine(principal.getId()).stream().map(ApprovalDelegationResponse::from).toList()));
    }

    @GetMapping("/delegated-to-me")
    public ResponseEntity<ApiResponse<List<ApprovalDelegationResponse>>> getDelegatedToMe(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                approvalDelegationService.getDelegatedToMe(principal.getId()).stream().map(ApprovalDelegationResponse::from).toList()));
    }

    @GetMapping("/pending-for-me")
    public ResponseEntity<ApiResponse<List<ApprovalDelegationResponse>>> getPendingForMe(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                approvalDelegationService.getPendingForManagerApproval(principal.getId()).stream().map(ApprovalDelegationResponse::from).toList()));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<ApprovalDelegationResponse>> approve(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        var delegation = approvalDelegationService.decide(id, principal.getId(), true);
        return ResponseEntity.ok(ApiResponse.success("Delegation approved", ApprovalDelegationResponse.from(delegation)));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<ApprovalDelegationResponse>> reject(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        var delegation = approvalDelegationService.decide(id, principal.getId(), false);
        return ResponseEntity.ok(ApiResponse.success("Delegation rejected", ApprovalDelegationResponse.from(delegation)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<ApprovalDelegationResponse>> cancel(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        var delegation = approvalDelegationService.cancel(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Delegation cancelled", ApprovalDelegationResponse.from(delegation)));
    }
}
