package com.rit.spms.controller;

import com.rit.spms.dto.request.CreateVsmAuthorGrantRequest;
import com.rit.spms.dto.response.ApiResponse;
import com.rit.spms.dto.response.VsmAuthorGrantResponse;
import com.rit.spms.security.UserPrincipal;
import com.rit.spms.service.VsmAuthorGrantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** VSM author delegation (Phase 4): Admin grants "VSM author" rights over a unit to an employee;
 *  the top-of-hierarchy head above that employee must approve before it's active. */
@RestController
@RequestMapping("/api/vsm/author-grants")
@RequiredArgsConstructor
public class VsmAuthorGrantController {

    private final VsmAuthorGrantService vsmAuthorGrantService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VsmAuthorGrantResponse>> createGrant(
            @Valid @RequestBody CreateVsmAuthorGrantRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        var grant = vsmAuthorGrantService.createGrant(req, principal.getId());
        return ResponseEntity.status(201).body(ApiResponse.success("Grant created, awaiting approval",
                VsmAuthorGrantResponse.from(grant)));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<VsmAuthorGrantResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(
                vsmAuthorGrantService.getAll().stream().map(VsmAuthorGrantResponse::from).toList()));
    }

    @PostMapping("/{id}/revoke")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VsmAuthorGrantResponse>> revoke(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        var grant = vsmAuthorGrantService.revoke(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Grant revoked", VsmAuthorGrantResponse.from(grant)));
    }

    @GetMapping("/pending-for-me")
    public ResponseEntity<ApiResponse<List<VsmAuthorGrantResponse>>> getPendingForMe(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(vsmAuthorGrantService
                .getPendingForApprover(principal.getId()).stream().map(VsmAuthorGrantResponse::from).toList()));
    }

    @GetMapping("/mine")
    public ResponseEntity<ApiResponse<List<VsmAuthorGrantResponse>>> getMine(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                vsmAuthorGrantService.getMine(principal.getId()).stream().map(VsmAuthorGrantResponse::from).toList()));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<VsmAuthorGrantResponse>> approve(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        var grant = vsmAuthorGrantService.decide(id, principal.getId(), true);
        return ResponseEntity.ok(ApiResponse.success("Grant approved", VsmAuthorGrantResponse.from(grant)));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<VsmAuthorGrantResponse>> reject(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        var grant = vsmAuthorGrantService.decide(id, principal.getId(), false);
        return ResponseEntity.ok(ApiResponse.success("Grant rejected", VsmAuthorGrantResponse.from(grant)));
    }
}
