package com.rit.spms.controller;

import com.rit.spms.domain.*;
import com.rit.spms.domain.enums.StrategyState;
import com.rit.spms.domain.enums.StrategyType;
import com.rit.spms.dto.request.RoleAssignmentRequest;
import com.rit.spms.dto.response.*;

import com.rit.spms.security.UserPrincipal;
import com.rit.spms.service.AdminService;
import com.rit.spms.service.CsvImportService;
import com.rit.spms.service.StrategyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final CsvImportService csvImportService;
    private final StrategyService strategyService;

    // --- Org Groups ---

    @GetMapping("/org-groups")
    public ResponseEntity<ApiResponse<List<OrgGroupResponse>>> getOrgGroups() {
        return ResponseEntity.ok(ApiResponse.success(
                adminService.getAllOrgGroups().stream().map(OrgGroupResponse::from).toList()));
    }

    @PostMapping("/org-groups")
    public ResponseEntity<ApiResponse<OrgGroupResponse>> createOrgGroup(
            @Valid @RequestBody OrgGroupRequest req) {
        OrgGroup g = adminService.createOrgGroup(
                req.getTitle(), req.getHeadTitle(), req.getParentId(), req.getHeadUserId());
        return ResponseEntity.status(201).body(ApiResponse.success("Group created", OrgGroupResponse.from(g)));
    }

    @PutMapping("/org-groups/{id}")
    public ResponseEntity<ApiResponse<OrgGroupResponse>> updateOrgGroup(
            @PathVariable Long id, @Valid @RequestBody OrgGroupRequest req) {
        OrgGroup g = adminService.updateOrgGroup(
                id, req.getTitle(), req.getHeadTitle(), req.getParentId(), req.getHeadUserId());
        return ResponseEntity.ok(ApiResponse.success(OrgGroupResponse.from(g)));
    }

    @DeleteMapping("/org-groups/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOrgGroup(@PathVariable Long id) {
        adminService.deleteOrgGroup(id);
        return ResponseEntity.ok(ApiResponse.success("Group deleted", null));
    }

    // --- Departments ---

    @GetMapping("/departments")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getDepartments() {
        return ResponseEntity.ok(ApiResponse.success(
                adminService.getAllDepartments().stream().map(DepartmentResponse::from).toList()));
    }

    @PostMapping("/departments")
    public ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(
            @Valid @RequestBody DepartmentRequest req) {
        Department dept = adminService.createDepartment(
                req.getName(), req.getCode(), req.getHeadTitle(),
                req.getHeadUserId(), req.getOrgGroupId());
        return ResponseEntity.status(201).body(ApiResponse.success("Department created", DepartmentResponse.from(dept)));
    }

    @PutMapping("/departments/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(
            @PathVariable Long id, @Valid @RequestBody DepartmentRequest req) {
        Department dept = adminService.updateDepartment(
                id, req.getName(), req.getCode(),
                req.getHeadTitle(), req.getHeadUserId(), req.getOrgGroupId());
        return ResponseEntity.ok(ApiResponse.success(DepartmentResponse.from(dept)));
    }

    @DeleteMapping("/departments/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivateDepartment(@PathVariable Long id) {
        adminService.deactivateDepartment(id);
        return ResponseEntity.ok(ApiResponse.success("Department deactivated", null));
    }

    @PatchMapping("/departments/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> reactivateDepartment(@PathVariable Long id) {
        adminService.reactivateDepartment(id);
        return ResponseEntity.ok(ApiResponse.success("Department activated", null));
    }

    // --- Users ---

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsers() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getAllUsers()));
    }

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest req) {
        UserResponse user = adminService.createUser(req.getFname(), req.getLname(), req.getEmail(),
                req.getTitle(), req.getDepartmentId(), Boolean.TRUE.equals(req.getIsAdmin()), req.getPassword());
        return ResponseEntity.status(201).body(ApiResponse.success("User created", user));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable Long id,
                                                                 @Valid @RequestBody UpdateUserRequest req) {
        UserResponse user = adminService.updateUser(id, req.getFname(), req.getLname(),
                req.getTitle(), req.getDepartmentId(), req.getIsAdmin());
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping("/users/import")
    public ResponseEntity<ApiResponse<CsvImportService.CsvImportResult>> importUsers(
            @RequestParam("file") MultipartFile file) {
        CsvImportService.CsvImportResult result = csvImportService.importUsers(file);
        return ResponseEntity.ok(ApiResponse.success("CSV import completed", result));
    }

    // --- Planning Cycles ---

    @GetMapping("/planning-cycles")
    public ResponseEntity<ApiResponse<List<PlanningCycle>>> getPlanningCycles() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getAllPlanningCycles()));
    }

    @PostMapping("/planning-cycles")
    public ResponseEntity<ApiResponse<PlanningCycle>> createPlanningCycle(
            @Valid @RequestBody PlanningCycleRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        PlanningCycle cycle = adminService.createPlanningCycle(
                req.getName(), req.getStartYear(), req.getEndYear(),
                req.getOwnerId(), principal.getId());
        return ResponseEntity.status(201).body(ApiResponse.success("Planning cycle created", cycle));
    }

    @PutMapping("/planning-cycles/{id}")
    public ResponseEntity<ApiResponse<Void>> updatePlanningCycle(@PathVariable Long id,
                                                                   @Valid @RequestBody PlanningCycleUpdateRequest req) {
        adminService.updatePlanningCycle(id, req.getName(), req.getStartYear(),
                req.getEndYear(), Boolean.TRUE.equals(req.getActive()));
        return ResponseEntity.ok(ApiResponse.success("Planning cycle updated", null));
    }

    @DeleteMapping("/planning-cycles/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePlanningCycle(@PathVariable Long id) {
        adminService.deletePlanningCycle(id);
        return ResponseEntity.ok(ApiResponse.success("Planning cycle deleted", null));
    }

    // --- Assessment Periods ---

    @GetMapping("/planning-cycles/{cycleId}/periods")
    public ResponseEntity<ApiResponse<List<AssessmentPeriod>>> getPeriods(@PathVariable Long cycleId) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getPeriodsForCycle(cycleId)));
    }

    @PostMapping("/planning-cycles/{cycleId}/periods")
    public ResponseEntity<ApiResponse<AssessmentPeriod>> createPeriod(@PathVariable Long cycleId,
                                                                        @Valid @RequestBody AssessmentPeriodRequest req) {
        AssessmentPeriod period = adminService.createAssessmentPeriod(cycleId, req.getName(),
                req.getStartDate(), req.getEndDate(), req.getSortOrder() != null ? req.getSortOrder() : 0);
        return ResponseEntity.status(201).body(ApiResponse.success("Assessment period created", period));
    }

    @DeleteMapping("/periods/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePeriod(@PathVariable Long id) {
        adminService.deleteAssessmentPeriod(id);
        return ResponseEntity.ok(ApiResponse.success("Assessment period deleted", null));
    }

    // --- Achievement Types ---

    @GetMapping("/achievement-types")
    public ResponseEntity<ApiResponse<List<AchievementType>>> getAchievementTypes() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getAllAchievementTypes()));
    }

    @PostMapping("/achievement-types")
    public ResponseEntity<ApiResponse<AchievementType>> createAchievementType(
            @Valid @RequestBody AchievementTypeRequest req) {
        return ResponseEntity.status(201).body(
                ApiResponse.success("Achievement type created", adminService.createAchievementType(req.getName())));
    }

    @PutMapping("/achievement-types/{id}")
    public ResponseEntity<ApiResponse<AchievementType>> updateAchievementType(@PathVariable Long id,
                                                                                @Valid @RequestBody AchievementTypeUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.success(
                adminService.updateAchievementType(id, req.getName(), Boolean.TRUE.equals(req.getActive()))));
    }

    @DeleteMapping("/achievement-types/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAchievementType(@PathVariable Long id) {
        adminService.deleteAchievementType(id);
        return ResponseEntity.ok(ApiResponse.success("Achievement type deactivated", null));
    }

    // --- Strategies (admin view) ---

    @DeleteMapping("/strategies/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStrategy(@PathVariable Long id) {
        adminService.deleteStrategy(id);
        return ResponseEntity.ok(ApiResponse.success("Strategy deleted", null));
    }

    @PostMapping("/strategies/university")
    public ResponseEntity<ApiResponse<StrategyResponse>> createUniversityStrategy(
            @Valid @RequestBody UniversityStrategyRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        StrategyType sType = "UNIVERSITY".equalsIgnoreCase(req.getType())
                ? StrategyType.UNIVERSITY : StrategyType.UNIT;
        Strategy strategy = adminService.createUniversityStrategy(
                req.getPlanningCycleId(), req.getTitle(), req.getDescription(),
                req.getOwnerId(), sType, principal.getId());
        return ResponseEntity.status(201).body(ApiResponse.success("University strategy created",
                strategyService.buildStrategyResponse(strategy, false)));
    }

    @GetMapping("/strategies")
    public ResponseEntity<ApiResponse<List<StrategyResponse>>> getAllStrategies() {
        List<StrategyResponse> responses = adminService.getAllStrategies().stream()
                .map(s -> strategyService.buildStrategyResponse(s, false))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/strategies/{id}")
    public ResponseEntity<ApiResponse<StrategyResponse>> getStrategy(@PathVariable Long id) {
        Strategy strategy = adminService.getStrategyById(id);
        return ResponseEntity.ok(ApiResponse.success(
                strategyService.buildStrategyResponse(strategy, true)));
    }

    @PatchMapping("/strategies/{id}/state")
    public ResponseEntity<ApiResponse<StrategyResponse>> adminOverrideState(
            @PathVariable Long id,
            @Valid @RequestBody AdminStateRequest req) {
        Strategy strategy = adminService.adminOverrideState(id, req.getState());
        return ResponseEntity.ok(ApiResponse.success("State overridden",
                strategyService.buildStrategyResponse(strategy, false)));
    }

    @GetMapping("/strategies/{id}/assignments")
    public ResponseEntity<ApiResponse<List<RoleAssignmentResponse>>> getStrategyAssignments(@PathVariable Long id) {
        List<RoleAssignmentResponse> responses = adminService.getStrategyAssignments(id).stream()
                .map(ra -> RoleAssignmentResponse.builder()
                        .id(ra.getId())
                        .strategyId(ra.getStrategy().getId())
                        .strategyTitle(ra.getStrategy().getTitle())
                        .userId(ra.getUser().getId())
                        .userEmail(ra.getUser().getEmail())
                        .userName(ra.getUser().getFname() + " " + ra.getUser().getLname())
                        .role(ra.getRole())
                        .build())
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/users/{id}/assignments")
    public ResponseEntity<ApiResponse<List<RoleAssignmentResponse>>> getUserAssignments(@PathVariable Long id) {
        List<RoleAssignmentResponse> responses = adminService.getUserAssignments(id).stream()
                .map(ra -> RoleAssignmentResponse.builder()
                        .id(ra.getId())
                        .strategyId(ra.getStrategy().getId())
                        .strategyTitle(ra.getStrategy().getTitle())
                        .userId(ra.getUser().getId())
                        .userEmail(ra.getUser().getEmail())
                        .userName(ra.getUser().getFname() + " " + ra.getUser().getLname())
                        .role(ra.getRole())
                        .build())
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @DeleteMapping("/assignments/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAssignment(@PathVariable Long id) {
        adminService.deleteAssignment(id);
        return ResponseEntity.ok(ApiResponse.success("Assignment removed", null));
    }

    // --- Role Assignment ---

    @PostMapping("/strategies/{strategyId}/assign-role")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<RoleAssignmentResponse>> assignRole(@PathVariable Long strategyId,
                                                                           @Valid @RequestBody RoleAssignmentRequest req,
                                                                           @AuthenticationPrincipal UserPrincipal principal) {
        RoleAssignment ra = strategyService.assignRole(strategyId, req, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Role assigned", RoleAssignmentResponse.builder()
                .id(ra.getId())
                .strategyId(ra.getStrategy().getId())
                .strategyTitle(ra.getStrategy().getTitle())
                .userId(ra.getUser().getId())
                .userEmail(ra.getUser().getEmail())
                .userName(ra.getUser().getFname() + " " + ra.getUser().getLname())
                .role(ra.getRole())
                .build()));
    }

    // --- Reference data (accessible by all authenticated users) ---

    @GetMapping("/achievement-types/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<AchievementType>>> getAchievementTypesPublic() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getAllAchievementTypes()));
    }

    @GetMapping("/planning-cycles/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PlanningCycle>>> getPlanningCyclesPublic() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getAllPlanningCycles()));
    }

    @GetMapping("/planning-cycles/{cycleId}/periods/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<AssessmentPeriod>>> getPeriodsPublic(@PathVariable Long cycleId) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getPeriodsForCycle(cycleId)));
    }

    // --- Audit Logs ---

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAuditLogs(
            @RequestParam(required = false) Long strategyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<AuditLogResponse> logs = adminService.getAuditLogs(strategyId,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    // --- Request bodies ---

    @Data public static class OrgGroupRequest {
        @NotBlank private String title;
        @NotBlank private String headTitle;
        private Long parentId;
        private Long headUserId;
    }

    @Data public static class DepartmentRequest {
        @NotBlank private String name;
        @NotBlank private String code;
        private String headTitle;
        private Long headUserId;
        private Long orgGroupId;
    }

    @Data public static class CreateUserRequest {
        @NotBlank private String fname;
        @NotBlank private String lname;
        @NotBlank private String email;
        private String title;
        private Long departmentId;
        private Boolean isAdmin;
        private String password;
    }

    @Data public static class UpdateUserRequest {
        private String fname;
        private String lname;
        private String title;
        private Long departmentId;
        private Boolean isAdmin;
    }

    @Data public static class PlanningCycleRequest {
        @NotBlank private String name;
        @NotNull private Integer startYear;
        @NotNull private Integer endYear;
        @NotNull private Long ownerId;
    }

    @Data public static class PlanningCycleUpdateRequest {
        @NotBlank private String name;
        @NotNull private Integer startYear;
        @NotNull private Integer endYear;
        private Boolean active;
    }

    @Data public static class AssessmentPeriodRequest {
        @NotBlank private String name;
        @NotNull private LocalDate startDate;
        @NotNull private LocalDate endDate;
        private Integer sortOrder;
    }

    @Data public static class AchievementTypeRequest {
        @NotBlank private String name;
    }

    @Data public static class AchievementTypeUpdateRequest {
        @NotBlank private String name;
        private Boolean active;
    }

    @Data public static class AdminStateRequest {
        @NotNull private StrategyState state;
    }

    @Data public static class UniversityStrategyRequest {
        @NotNull private Long planningCycleId;
        @NotBlank private String title;
        private String description;
        @NotNull private Long ownerId;
        private String type; // "UNIVERSITY" (main aggregator) or "UNIT" (sub-unit); defaults to UNIT
    }
}
