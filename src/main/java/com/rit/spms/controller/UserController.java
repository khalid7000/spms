package com.rit.spms.controller;

import com.rit.spms.domain.Department;
import com.rit.spms.domain.OrgGroup;
import com.rit.spms.dto.response.ApiResponse;
import com.rit.spms.dto.response.LeadershipProfileResponse;
import com.rit.spms.dto.response.UserResponse;
import com.rit.spms.repository.AppUserRepository;
import com.rit.spms.repository.DepartmentRepository;
import com.rit.spms.repository.OrgGroupRepository;
import com.rit.spms.security.UserPrincipal;
import com.rit.spms.service.EmployeeGoalCycleService;
import com.rit.spms.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final AppUserRepository appUserRepository;
    private final DepartmentRepository departmentRepository;
    private final OrgGroupRepository orgGroupRepository;
    private final EmployeeGoalCycleService employeeGoalCycleService;
    private final PermissionService permissionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> searchUsers(
            @RequestParam(defaultValue = "") String q) {
        List<UserResponse> results = appUserRepository
                .searchActive(q, PageRequest.of(0, 30))
                .stream().map(UserResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    /**
     * Drives Strategy Creation Console visibility/options: department heads can create a
     * department strategy for what they head; only the root Org Group's head can create the
     * university strategy. hasDirectReports uses the same "direct reports" definition as Team Goal
     * Setting/Team Evaluations (EmployeeGoalCycleService#getDirectReports) -- not just
     * headedDepartments -- so an org-group head (e.g. a Dean/Provost) whose only "reports" are
     * department heads under their hierarchy (rather than a department of their own) still sees
     * those tools. hasMultiLevelHierarchy gates Organization Evaluations: it's only worth its own
     * console when the hierarchy includes departments beyond the ones this user heads directly --
     * otherwise it shows exactly the same people as Team Evaluations.
     */
    @GetMapping("/me/leadership")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LeadershipProfileResponse>> getMyLeadership(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<Department> departments = departmentRepository.findByHeadId(principal.getId());
        List<OrgGroup> orgGroups = orgGroupRepository.findByHeadId(principal.getId());
        int hierarchySize = permissionService.resolveHierarchyDepartmentIds(principal.getId()).size();

        LeadershipProfileResponse response = LeadershipProfileResponse.builder()
                .headedDepartments(departments.stream()
                        .map(d -> new LeadershipProfileResponse.DeptInfo(d.getId(), d.getName()))
                        .toList())
                .headedOrgGroups(orgGroups.stream()
                        .map(g -> new LeadershipProfileResponse.OrgGroupInfo(g.getId(), g.getTitle(), g.getParent() == null))
                        .toList())
                .hasDirectReports(!employeeGoalCycleService.getDirectReports(principal.getId()).isEmpty())
                .hasMultiLevelHierarchy(hierarchySize > departments.size())
                .build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
