package com.rit.spms.dto.response;

import com.rit.spms.domain.ImprovementTask;
import com.rit.spms.domain.ImprovementTaskAssignee;
import com.rit.spms.domain.enums.VsmTaskState;
import com.rit.spms.domain.enums.VsmTaskType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ImprovementTaskResponse {
    private Long id;
    private Long kaizenNodeId;
    private String kaizenNodeTitle;
    private Long vsmMapId;
    private String vsmMapTitle;
    private String title;
    private String description;
    private VsmTaskType taskType;
    private VsmTaskState state;
    private String createdByName;
    private Long pulledById;
    private String pulledByName;
    private LocalDateTime pulledAt;
    private LocalDateTime doneAt;
    private LocalDateTime createdAt;

    // Phase 4: lets the frontend show "Log Achievement" vs "Complete" for an IMPROVEMENT task
    // without a separate call, and shows what it's linked to once set.
    private boolean achievementRequired;
    private Long achievementId;
    private Long linkedInitiativeId;
    private String linkedInitiativeTitle;

    /** Whether the *current viewer* is this task's map's author/admin -- lets the board show a
     *  "change type" action to the right audience without duplicating that rule on the frontend.
     *  False/empty by default (see {@link #from(ImprovementTask)}); board-listing and task-detail
     *  call sites that actually render these actions pass the real values via the full {@link
     *  #from(ImprovementTask, boolean, List, long, boolean, boolean)}. */
    private boolean canEditMap;

    /** Collaborators added to this task (see ImprovementTaskAssignee) -- can view/add notes, but
     *  never change state. Wiped whenever the map's author returns the task to the board. */
    private List<AssigneeInfo> assignees;

    /** How many notes exist on this task -- lets a task list highlight "has activity" without a
     *  separate call per card. */
    private long noteCount;

    /** Whether the *current viewer* may add a note: the task's owner, a collaborator, or the map's
     *  author/admin (who can note any task on their map at any time). */
    private boolean canAddNote;

    /** Whether the *current viewer* may add/remove collaborators: the task's owner, or the map's
     *  author/admin. */
    private boolean canManageAssignees;

    public record AssigneeInfo(Long employeeId, String employeeName) {
    }

    public static ImprovementTaskResponse from(ImprovementTask task) {
        return from(task, false, List.of(), 0, false, false);
    }

    public static ImprovementTaskResponse from(ImprovementTask task, boolean canEditMap) {
        return from(task, canEditMap, List.of(), 0, false, false);
    }

    public static ImprovementTaskResponse from(
            ImprovementTask task, boolean canEditMap, List<ImprovementTaskAssignee> assignees,
            long noteCount, boolean canAddNote, boolean canManageAssignees) {
        return ImprovementTaskResponse.builder()
                .canEditMap(canEditMap)
                .assignees(assignees.stream()
                        .map(a -> new AssigneeInfo(a.getEmployee().getId(),
                                a.getEmployee().getFname() + " " + a.getEmployee().getLname()))
                        .toList())
                .noteCount(noteCount)
                .canAddNote(canAddNote)
                .canManageAssignees(canManageAssignees)
                .id(task.getId())
                .kaizenNodeId(task.getKaizenNode().getId())
                .kaizenNodeTitle(task.getKaizenNode().getTitle())
                .vsmMapId(task.getKaizenNode().getVsmMap().getId())
                .vsmMapTitle(task.getKaizenNode().getVsmMap().getTitle())
                .title(task.getTitle())
                .description(task.getDescription())
                .taskType(task.getTaskType())
                .state(task.getState())
                .createdByName(task.getCreatedBy().getFname() + " " + task.getCreatedBy().getLname())
                .pulledById(task.getPulledBy() != null ? task.getPulledBy().getId() : null)
                .pulledByName(task.getPulledBy() != null
                        ? task.getPulledBy().getFname() + " " + task.getPulledBy().getLname() : null)
                .pulledAt(task.getPulledAt())
                .doneAt(task.getDoneAt())
                .createdAt(task.getCreatedAt())
                .achievementRequired(task.getTaskType() == com.rit.spms.domain.enums.VsmTaskType.IMPROVEMENT
                        && task.getAchievement() == null)
                .achievementId(task.getAchievement() != null ? task.getAchievement().getId() : null)
                .linkedInitiativeId(task.getLinkedInitiative() != null ? task.getLinkedInitiative().getId() : null)
                .linkedInitiativeTitle(task.getLinkedInitiative() != null ? task.getLinkedInitiative().getTitle() : null)
                .build();
    }
}
