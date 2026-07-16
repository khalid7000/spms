package com.rit.spms.controller;

import com.rit.spms.domain.Strategy;
import com.rit.spms.domain.enums.SystemRole;
import com.rit.spms.dto.request.ChangeStateRequest;
import com.rit.spms.dto.request.CreateStrategyRequest;
import com.rit.spms.dto.request.RoleAssignmentRequest;
import com.rit.spms.dto.request.SetThresholdRequest;
import com.rit.spms.dto.response.ApiResponse;
import com.rit.spms.dto.response.AuditLogResponse;
import com.rit.spms.dto.response.RoleAssignmentResponse;
import com.rit.spms.dto.response.StrategyResponse;
import com.rit.spms.exception.UnauthorizedException;
import com.rit.spms.security.UserPrincipal;
import com.rit.spms.service.AdminService;
import com.rit.spms.service.ExcelExportService;
import com.rit.spms.service.PermissionService;
import com.rit.spms.service.PdfExportService;
import com.rit.spms.service.StrategyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Strategy CRUD, role assignments, state transitions, and audit-log/report access for a single strategy. */
@RestController
@RequestMapping("/api/strategies")
@RequiredArgsConstructor
public class StrategyController {

    private final StrategyService strategyService;
    private final PdfExportService pdfExportService;
    private final ExcelExportService excelExportService;
    private final AdminService adminService;
    private final PermissionService permissionService;

    @PostMapping("/university")
    public ResponseEntity<ApiResponse<StrategyResponse>> createUniversityStrategy(
            @Valid @RequestBody CreateStrategyRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        Strategy strategy = strategyService.createUniversityStrategy(req, principal.getId());
        return ResponseEntity.status(201).body(
                ApiResponse.success("University strategy created",
                        strategyService.buildStrategyResponse(strategy, false)));
    }

    @PostMapping("/department")
    public ResponseEntity<ApiResponse<StrategyResponse>> createDepartmentStrategy(
            @Valid @RequestBody CreateStrategyRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        Strategy strategy = strategyService.createDepartmentStrategy(req, principal.getId());
        return ResponseEntity.status(201).body(
                ApiResponse.success("Department strategy created",
                        strategyService.buildStrategyResponse(strategy, false)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StrategyResponse>> getStrategy(
            @PathVariable Long id,
            @RequestParam(required = false) Long academicYearId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                strategyService.getStrategy(id, principal.getId(), academicYearId)));
    }

    @PatchMapping("/{id}/state")
    public ResponseEntity<ApiResponse<StrategyResponse>> changeState(
            @PathVariable Long id,
            @Valid @RequestBody ChangeStateRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        Strategy strategy = strategyService.changeState(id, req, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("State changed",
                strategyService.buildStrategyResponse(strategy, false)));
    }

    @PatchMapping("/{id}/threshold")
    public ResponseEntity<ApiResponse<StrategyResponse>> setThreshold(
            @PathVariable Long id,
            @Valid @RequestBody SetThresholdRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        Strategy strategy = strategyService.setThreshold(id, req, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Threshold updated",
                strategyService.buildStrategyResponse(strategy, false)));
    }

    /** "Recently logged" feed for the achievement rail -- open to any strategy member, not just owners/admins. */
    @GetMapping("/{id}/achievements/recent")
    public ResponseEntity<ApiResponse<List<com.rit.spms.dto.response.RecentAchievementResponse>>> getRecentAchievements(
            @PathVariable Long id,
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                strategyService.getRecentAchievements(id, principal.getId(), limit)));
    }

    @GetMapping("/{id}/audit-log")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAuditLog(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal UserPrincipal principal) {
        boolean isAdmin = principal.hasRole(SystemRole.ADMIN);
        boolean isOwner = permissionService.isOwner(principal.getId(), id);
        if (!isAdmin && !isOwner) {
            throw new UnauthorizedException("Only strategy owners can view the audit log");
        }
        Page<AuditLogResponse> logs = adminService.getStrategyAuditLog(
                id, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        byte[] pdf = pdfExportService.exportStrategy(id, principal.getId());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"strategy-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<ApiResponse<List<RoleAssignmentResponse>>> getMembers(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                strategyService.getMembers(id, principal.getId())));
    }

    @PutMapping("/{id}/members")
    public ResponseEntity<ApiResponse<RoleAssignmentResponse>> assignMember(
            @PathVariable Long id,
            @Valid @RequestBody RoleAssignmentRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Member updated",
                strategyService.assignMemberRole(id, req, principal.getId())));
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> revokeMember(
            @PathVariable Long id,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal principal) {
        strategyService.revokeRole(id, userId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Access revoked", null));
    }

    @GetMapping("/{id}/excel")
    public ResponseEntity<byte[]> downloadExcel(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        byte[] excel = excelExportService.exportStrategy(id, principal.getId());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"strategy-" + id + ".xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }
}
