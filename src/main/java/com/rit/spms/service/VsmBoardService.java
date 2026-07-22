package com.rit.spms.service;

import com.rit.spms.domain.ImprovementTask;
import com.rit.spms.domain.ImprovementTaskAssignee;
import com.rit.spms.domain.VsmMap;
import com.rit.spms.domain.enums.VsmTaskState;
import com.rit.spms.dto.response.ImprovementTaskResponse;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.repository.ImprovementTaskAssigneeRepository;
import com.rit.spms.repository.ImprovementTaskNoteRepository;
import com.rit.spms.repository.ImprovementTaskRepository;
import com.rit.spms.repository.VsmMapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * "Per-map board" and "department rollup" are both just filtered/grouped queries over
 * ImprovementTask rows -- there is no separate KanbanBoard entity (see the round-1 VSM plan's
 * judgment call #3). This service is the seam: if a second task-producing source ever appears,
 * only this class's internals change, not the controller contract or the frontend.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VsmBoardService {

    private static final List<VsmTaskState> BOARD_STATES =
            List.of(VsmTaskState.AVAILABLE, VsmTaskState.PULLED, VsmTaskState.IN_PROGRESS, VsmTaskState.DONE);
    private static final List<VsmTaskState> BOARD_STATES_WITH_BACKLOG =
            List.of(VsmTaskState.BACKLOG, VsmTaskState.AVAILABLE, VsmTaskState.PULLED,
                    VsmTaskState.IN_PROGRESS, VsmTaskState.DONE);

    private final ImprovementTaskRepository improvementTaskRepository;
    private final ImprovementTaskAssigneeRepository improvementTaskAssigneeRepository;
    private final ImprovementTaskNoteRepository improvementTaskNoteRepository;
    private final VsmMapRepository vsmMapRepository;
    private final PermissionService permissionService;

    /** A single map's board -- its author/an admin also sees their own not-yet-published BACKLOG
     *  tasks; anyone else sees only what's actually been published. */
    public List<ImprovementTaskResponse> getMapBoard(Long mapId, Long userId) {
        VsmMap map = vsmMapRepository.findById(mapId)
                .orElseThrow(() -> new ResourceNotFoundException("VsmMap", mapId));
        permissionService.assertCanViewVsmMap(userId, map);
        boolean canEditMap = permissionService.canEditVsmMap(userId, map);
        List<VsmTaskState> states = canEditMap ? BOARD_STATES_WITH_BACKLOG : BOARD_STATES;
        List<ImprovementTask> tasks = improvementTaskRepository
                .findByKaizenNode_VsmMap_IdAndStateInOrderByCreatedAtDesc(mapId, states);
        return toResponses(tasks, userId, task -> canEditMap);
    }

    /** Every open task across every map owned within a department -- lets faculty browse everything
     *  available to pull in one place, regardless of which specific process map it came from.
     *  BACKLOG tasks never appear here (that's each map's own author-only view). */
    public List<ImprovementTaskResponse> getDepartmentBoard(Long departmentId, Long userId) {
        permissionService.assertCanViewDepartmentBoard(userId, departmentId);
        List<ImprovementTask> tasks = improvementTaskRepository
                .findByKaizenNode_VsmMap_Department_IdAndStateInOrderByCreatedAtDesc(departmentId, BOARD_STATES);
        // A department rollup can span several different maps/authors, so -- unlike the single-map
        // board above -- canEditMap has to be resolved per task rather than once for the whole list.
        return toResponses(tasks, userId, task -> permissionService.canEditVsmMap(userId, task.getKaizenNode().getVsmMap()));
    }

    /** Shared response-building for both board listings above: bulk-fetches every task's
     *  collaborators and note count in two queries total (not one pair per row), then derives
     *  canAddNote/canManageAssignees per task for this specific viewer. */
    private List<ImprovementTaskResponse> toResponses(
            List<ImprovementTask> tasks, Long userId, java.util.function.Function<ImprovementTask, Boolean> canEditMapFor) {
        List<Long> taskIds = tasks.stream().map(ImprovementTask::getId).toList();
        Map<Long, List<ImprovementTaskAssignee>> assigneesByTask = taskIds.isEmpty() ? Map.of()
                : improvementTaskAssigneeRepository.findByImprovementTaskIdIn(taskIds).stream()
                        .collect(Collectors.groupingBy(a -> a.getImprovementTask().getId()));
        Map<Long, Long> noteCountByTask = taskIds.isEmpty() ? Map.of()
                : improvementTaskNoteRepository.findByImprovementTaskIdIn(taskIds).stream()
                        .collect(Collectors.groupingBy(n -> n.getImprovementTask().getId(), Collectors.counting()));

        return tasks.stream().map(task -> {
            boolean canEditMap = canEditMapFor.apply(task);
            List<ImprovementTaskAssignee> assignees = assigneesByTask.getOrDefault(task.getId(), List.of());
            long noteCount = noteCountByTask.getOrDefault(task.getId(), 0L);
            boolean isOwner = task.getPulledBy() != null && task.getPulledBy().getId().equals(userId);
            boolean canManageAssignees = canEditMap || isOwner;
            boolean isAssignee = assignees.stream().anyMatch(a -> a.getEmployee().getId().equals(userId));
            boolean canAddNote = canManageAssignees || isAssignee;
            return ImprovementTaskResponse.from(task, canEditMap, assignees, noteCount, canAddNote, canManageAssignees);
        }).toList();
    }
}
