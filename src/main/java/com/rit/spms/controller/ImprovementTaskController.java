package com.rit.spms.controller;

import com.rit.spms.dto.request.AddTaskAssigneeRequest;
import com.rit.spms.dto.request.CreateImprovementTaskRequest;
import com.rit.spms.dto.request.CreateTaskNoteRequest;
import com.rit.spms.dto.request.LogTaskAchievementRequest;
import com.rit.spms.dto.request.UpdateTaskTypeRequest;
import com.rit.spms.dto.response.AchievableMeasurementResponse;
import com.rit.spms.dto.response.ApiResponse;
import com.rit.spms.dto.response.ImprovementTaskNoteResponse;
import com.rit.spms.dto.response.ImprovementTaskResponse;
import com.rit.spms.dto.response.VsmDashboardResponse;
import com.rit.spms.security.UserPrincipal;
import com.rit.spms.service.ImprovementTaskService;
import com.rit.spms.service.VsmBoardService;
import com.rit.spms.service.VsmDashboardService;
import com.rit.spms.service.VsmTaskAchievementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Kaizen-burst improvement tasks: create/publish (map author), pull/start/complete (whoever can
 *  see the map), achievement-logging for IMPROVEMENT tasks (Phase 4), notes/collaborators, the two
 *  board views (per-map, department rollup), and the central "my dashboard" summary. */
@RestController
@RequestMapping("/api/vsm")
@RequiredArgsConstructor
public class ImprovementTaskController {

    private final ImprovementTaskService improvementTaskService;
    private final VsmBoardService vsmBoardService;
    private final VsmTaskAchievementService vsmTaskAchievementService;
    private final VsmDashboardService vsmDashboardService;

    @PostMapping("/nodes/{nodeId}/tasks")
    public ResponseEntity<ApiResponse<ImprovementTaskResponse>> createTask(
            @PathVariable Long nodeId,
            @Valid @RequestBody CreateImprovementTaskRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        var task = improvementTaskService.createTask(nodeId, req, principal.getId());
        return ResponseEntity.status(201).body(ApiResponse.success("Task created", ImprovementTaskResponse.from(task)));
    }

    @PostMapping("/tasks/{id}/publish")
    public ResponseEntity<ApiResponse<ImprovementTaskResponse>> publishTask(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        var task = improvementTaskService.publish(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Task published to board", ImprovementTaskResponse.from(task)));
    }

    @PostMapping("/tasks/{id}/pull")
    public ResponseEntity<ApiResponse<ImprovementTaskResponse>> pullTask(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        var task = improvementTaskService.pull(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Task pulled", ImprovementTaskResponse.from(task)));
    }

    @PostMapping("/tasks/{id}/start")
    public ResponseEntity<ApiResponse<ImprovementTaskResponse>> startTask(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        var task = improvementTaskService.start(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Task started", ImprovementTaskResponse.from(task)));
    }

    @PostMapping("/tasks/{id}/complete")
    public ResponseEntity<ApiResponse<ImprovementTaskResponse>> completeTask(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        var task = improvementTaskService.complete(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Task completed", ImprovementTaskResponse.from(task)));
    }

    /** Only the map's author/admin, and only before the task is DONE -- reclassifies it between
     *  MINOR and IMPROVEMENT while it's still moving through the team. */
    @PutMapping("/tasks/{id}/type")
    public ResponseEntity<ApiResponse<ImprovementTaskResponse>> changeTaskType(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskTypeRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        var task = improvementTaskService.changeTaskType(id, req.getTaskType(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Task type updated", ImprovementTaskResponse.from(task)));
    }

    /** Only the map's author/admin, and only while PULLED/IN_PROGRESS -- takes the task back from
     *  whoever claimed it and returns it to the board as AVAILABLE. */
    @PostMapping("/tasks/{id}/return-to-board")
    public ResponseEntity<ApiResponse<ImprovementTaskResponse>> returnTaskToBoard(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        var task = improvementTaskService.returnToBoard(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Task returned to the board", ImprovementTaskResponse.from(task)));
    }

    /** Every Measurement the current user can log an achievement against -- backs the picker in the
     *  "log achievement to complete an Improvement task" flow. */
    @GetMapping("/achievable-measurements")
    public ResponseEntity<ApiResponse<List<AchievableMeasurementResponse>>> getAchievableMeasurements(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(vsmTaskAchievementService.getAchievableMeasurements(principal.getId())));
    }

    /** Logs the achievement required to complete an IMPROVEMENT task -- #complete then allows DONE. */
    @PostMapping("/tasks/{id}/achievement")
    public ResponseEntity<ApiResponse<ImprovementTaskResponse>> logTaskAchievement(
            @PathVariable Long id,
            @Valid @RequestBody LogTaskAchievementRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        var task = vsmTaskAchievementService.logAchievementForTask(id, req, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Achievement logged", ImprovementTaskResponse.from(task)));
    }

    /** Full detail for the task-progress page -- anyone who can view the map (its author, an admin,
     *  or a same-department viewer) can see it; owner/collaborator/head-only actions are surfaced
     *  as canAddNote/canManageAssignees/canEditMap booleans on the response. */
    @GetMapping("/tasks/{id}")
    public ResponseEntity<ApiResponse<ImprovementTaskResponse>> getTaskDetail(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(improvementTaskService.getTaskDetail(id, principal.getId())));
    }

    /** Owner, a current collaborator, or the map's author/admin -- permanent, author-attributed,
     *  never edited/deleted. */
    @PostMapping("/tasks/{id}/notes")
    public ResponseEntity<ApiResponse<ImprovementTaskNoteResponse>> addTaskNote(
            @PathVariable Long id,
            @Valid @RequestBody CreateTaskNoteRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        var note = improvementTaskService.addNote(id, req.getBody(), principal.getId());
        return ResponseEntity.status(201).body(ApiResponse.success("Note added", ImprovementTaskNoteResponse.from(note)));
    }

    @GetMapping("/tasks/{id}/notes")
    public ResponseEntity<ApiResponse<List<ImprovementTaskNoteResponse>>> getTaskNotes(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(improvementTaskService.getNotes(id, principal.getId())));
    }

    /** Only the task's owner or the map's author/admin, and only while PULLED/IN_PROGRESS -- the
     *  added employee can view the task and add notes but never change its state. */
    @PostMapping("/tasks/{id}/assignees")
    public ResponseEntity<ApiResponse<ImprovementTaskResponse>> addTaskAssignee(
            @PathVariable Long id,
            @Valid @RequestBody AddTaskAssigneeRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        improvementTaskService.addAssignee(id, req.getEmployeeId(), principal.getId());
        return ResponseEntity.status(201).body(ApiResponse.success(
                "Collaborator added", improvementTaskService.getTaskDetail(id, principal.getId())));
    }

    @DeleteMapping("/tasks/{id}/assignees/{employeeId}")
    public ResponseEntity<ApiResponse<ImprovementTaskResponse>> removeTaskAssignee(
            @PathVariable Long id, @PathVariable Long employeeId,
            @AuthenticationPrincipal UserPrincipal principal) {
        improvementTaskService.removeAssignee(id, employeeId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(
                "Collaborator removed", improvementTaskService.getTaskDetail(id, principal.getId())));
    }

    /** The central "My Tasks" dashboard payload: available-to-pull preview for the viewer's own
     *  department, plus every task they own or collaborate on across every map. */
    @GetMapping("/my-dashboard")
    public ResponseEntity<ApiResponse<VsmDashboardResponse>> getMyDashboard(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(vsmDashboardService.getMyDashboard(principal.getId())));
    }

    @GetMapping("/maps/{mapId}/board")
    public ResponseEntity<ApiResponse<List<ImprovementTaskResponse>>> getMapBoard(
            @PathVariable Long mapId, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(vsmBoardService.getMapBoard(mapId, principal.getId())));
    }

    @GetMapping("/departments/{departmentId}/board")
    public ResponseEntity<ApiResponse<List<ImprovementTaskResponse>>> getDepartmentBoard(
            @PathVariable Long departmentId, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(vsmBoardService.getDepartmentBoard(departmentId, principal.getId())));
    }
}
