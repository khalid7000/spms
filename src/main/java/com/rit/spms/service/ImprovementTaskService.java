package com.rit.spms.service;

import com.rit.spms.domain.AppUser;
import com.rit.spms.domain.ImprovementTask;
import com.rit.spms.domain.ImprovementTaskAssignee;
import com.rit.spms.domain.ImprovementTaskNote;
import com.rit.spms.domain.Initiative;
import com.rit.spms.domain.VsmMap;
import com.rit.spms.domain.VsmNode;
import com.rit.spms.domain.enums.SystemRole;
import com.rit.spms.domain.enums.VsmNodeType;
import com.rit.spms.domain.enums.VsmTaskState;
import com.rit.spms.domain.enums.VsmTaskType;
import com.rit.spms.dto.request.CreateImprovementTaskRequest;
import com.rit.spms.dto.response.ImprovementTaskNoteResponse;
import com.rit.spms.dto.response.ImprovementTaskResponse;
import com.rit.spms.exception.BusinessRuleException;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.exception.UnauthorizedException;
import com.rit.spms.repository.AppUserRepository;
import com.rit.spms.repository.ImprovementTaskAssigneeRepository;
import com.rit.spms.repository.ImprovementTaskNoteRepository;
import com.rit.spms.repository.ImprovementTaskRepository;
import com.rit.spms.repository.InitiativeRepository;
import com.rit.spms.repository.VsmNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Create/publish/pull/start/complete for Kaizen-burst improvement tasks (Phase 3 of the VSM
 * module), plus the Phase 4 achievement-linkage gate: an IMPROVEMENT task cannot reach DONE without
 * a real Achievement already attached (see {@link VsmTaskAchievementService}, which sets it) --
 * MINOR tasks are unaffected and still close directly. Also owns collaborator (Section: "share a
 * task") and note-taking on top of a task, both scoped independently of the Kanban state machine
 * above.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ImprovementTaskService {

    private final ImprovementTaskRepository improvementTaskRepository;
    private final ImprovementTaskNoteRepository improvementTaskNoteRepository;
    private final ImprovementTaskAssigneeRepository improvementTaskAssigneeRepository;
    private final VsmNodeRepository vsmNodeRepository;
    private final AppUserRepository appUserRepository;
    private final InitiativeRepository initiativeRepository;
    private final PermissionService permissionService;
    private final ApplicationEventPublisher eventPublisher;

    /** Only the map's author/admin, and only on a KAIZEN_BURST node -- created BACKLOG, invisible
     *  to anyone else until {@link #publish}. */
    public ImprovementTask createTask(Long kaizenNodeId, CreateImprovementTaskRequest req, Long userId) {
        VsmNode node = requireNode(kaizenNodeId);
        permissionService.assertCanEditVsmMap(userId, node.getVsmMap());
        if (node.getNodeType() != VsmNodeType.KAIZEN_BURST) {
            throw new BusinessRuleException("Improvement tasks can only be created on a Kaizen Burst node");
        }
        AppUser creator = requireUser(userId);
        ImprovementTask.ImprovementTaskBuilder builder = ImprovementTask.builder()
                .kaizenNode(node)
                .title(req.getTitle())
                .description(req.getDescription())
                .taskType(req.getTaskType())
                .state(VsmTaskState.BACKLOG)
                .createdBy(creator);
        if (req.getLinkedInitiativeId() != null) {
            Initiative initiative = initiativeRepository.findById(req.getLinkedInitiativeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Initiative", req.getLinkedInitiativeId()));
            builder.linkedInitiative(initiative);
        }
        return improvementTaskRepository.save(builder.build());
    }

    /** BACKLOG -> AVAILABLE: publishes a leader's draft task to the Kanban board. */
    public ImprovementTask publish(Long taskId, Long userId) {
        ImprovementTask task = requireTask(taskId);
        permissionService.assertCanEditVsmMap(userId, task.getKaizenNode().getVsmMap());
        if (task.getState() != VsmTaskState.BACKLOG) {
            throw new BusinessRuleException("Only a backlog task can be published");
        }
        task.setState(VsmTaskState.AVAILABLE);
        return improvementTaskRepository.save(task);
    }

    /** AVAILABLE -> PULLED: self-assigns the task. Anyone who can see the map (its author, an
     *  admin, or -- for a department-scoped map -- anyone in that department) can pull. */
    public ImprovementTask pull(Long taskId, Long userId) {
        ImprovementTask task = requireTask(taskId);
        permissionService.assertCanViewVsmMap(userId, task.getKaizenNode().getVsmMap());
        if (task.getState() != VsmTaskState.AVAILABLE) {
            throw new BusinessRuleException("This task is not available to pull");
        }
        AppUser puller = requireUser(userId);
        task.setPulledBy(puller);
        task.setPulledAt(LocalDateTime.now());
        task.setState(VsmTaskState.PULLED);
        ImprovementTask saved = improvementTaskRepository.save(task);
        eventPublisher.publishEvent(new VsmTaskPulledEvent(saved.getId()));
        return saved;
    }

    /** PULLED -> IN_PROGRESS: only the person who pulled it, or an admin. */
    public ImprovementTask start(Long taskId, Long userId) {
        ImprovementTask task = requireTask(taskId);
        assertIsPullerOrAdmin(task, userId);
        if (task.getState() != VsmTaskState.PULLED) {
            throw new BusinessRuleException("Only a pulled task can be started");
        }
        task.setState(VsmTaskState.IN_PROGRESS);
        return improvementTaskRepository.save(task);
    }

    /** PULLED or IN_PROGRESS -> DONE: only the person who pulled it, or an admin. An IMPROVEMENT
     *  task additionally requires a real Achievement already attached (see
     *  VsmTaskAchievementService#logAchievementForTask) -- MINOR tasks have no such requirement. */
    public ImprovementTask complete(Long taskId, Long userId) {
        ImprovementTask task = requireTask(taskId);
        assertIsPullerOrAdmin(task, userId);
        if (task.getState() != VsmTaskState.PULLED && task.getState() != VsmTaskState.IN_PROGRESS) {
            throw new BusinessRuleException("Only a pulled or in-progress task can be completed");
        }
        if (task.getTaskType() == VsmTaskType.IMPROVEMENT && task.getAchievement() == null) {
            throw new BusinessRuleException(
                    "This is an Improvement task -- log an achievement for it before marking it done");
        }
        task.setState(VsmTaskState.DONE);
        task.setDoneAt(LocalDateTime.now());
        ImprovementTask saved = improvementTaskRepository.save(task);
        eventPublisher.publishEvent(new VsmTaskCompletedEvent(saved.getId()));
        return saved;
    }

    /**
     * Lets the map's author (or an admin) reclassify a task between MINOR and IMPROVEMENT while
     * it's still in flight -- e.g. a task originally logged as a quick fix turns out to need a real
     * achievement once someone's actually working it, or vice versa. Blocked once DONE: a MINOR
     * task that already closed was never required to have an achievement, and retroactively
     * flipping it to IMPROVEMENT after the fact would leave that invariant broken with no one left
     * in the workflow to satisfy it. Deliberately leaves any already-logged achievement attached
     * either way -- real portfolio work already recorded isn't undone by a reclassification.
     */
    public ImprovementTask changeTaskType(Long taskId, VsmTaskType newType, Long userId) {
        ImprovementTask task = requireTask(taskId);
        permissionService.assertCanEditVsmMap(userId, task.getKaizenNode().getVsmMap());
        if (task.getState() == VsmTaskState.DONE) {
            throw new BusinessRuleException("Cannot change a task's type once it's already done");
        }
        task.setTaskType(newType);
        return improvementTaskRepository.save(task);
    }

    /**
     * Lets the map's author (or admin) pull a task back off whoever claimed it -- e.g. the assignee
     * went quiet or over-committed -- and return it to the board as AVAILABLE for someone else to
     * pick up. Only valid while PULLED or IN_PROGRESS: a BACKLOG/AVAILABLE task has no one to take
     * it back from, and a DONE task is already closed. Clears pulledBy/pulledAt and every
     * collaborator link (see {@link ImprovementTaskAssignee}) -- everyone loses their link to the
     * task -- but leaves taskType/achievement/notes untouched, same "don't undo real state"
     * reasoning as {@link #changeTaskType}: a note is a permanent, author-attributed record, not a
     * property of who currently holds the task.
     */
    public ImprovementTask returnToBoard(Long taskId, Long userId) {
        ImprovementTask task = requireTask(taskId);
        permissionService.assertCanEditVsmMap(userId, task.getKaizenNode().getVsmMap());
        if (task.getState() != VsmTaskState.PULLED && task.getState() != VsmTaskState.IN_PROGRESS) {
            throw new BusinessRuleException("Only a pulled or in-progress task can be returned to the board");
        }
        AppUser previousPuller = task.getPulledBy();
        task.setState(VsmTaskState.AVAILABLE);
        task.setPulledBy(null);
        task.setPulledAt(null);
        ImprovementTask saved = improvementTaskRepository.save(task);
        improvementTaskAssigneeRepository.deleteByImprovementTaskId(taskId);
        if (previousPuller != null) {
            eventPublisher.publishEvent(new VsmTaskReturnedToBoardEvent(saved.getId(), previousPuller.getId()));
        }
        return saved;
    }

    /**
     * Adds a note to a task -- the owner, any current collaborator, or the map's author/admin (who
     * may note any task on their own map at any time, regardless of whether it's even assigned).
     * Notes are a permanent, append-only, author-attributed log: no edit/delete, and reassigning or
     * stripping the task never touches them.
     */
    public ImprovementTaskNote addNote(Long taskId, String body, Long userId) {
        ImprovementTask task = requireTask(taskId);
        AppUser author = requireUser(userId);
        if (!canAddNote(task, author)) {
            throw new UnauthorizedException(
                    "Only this task's owner, a collaborator on it, or the map's author/admin can add a note");
        }
        return improvementTaskNoteRepository.save(ImprovementTaskNote.builder()
                .improvementTask(task)
                .author(author)
                .body(body)
                .build());
    }

    public List<ImprovementTaskNoteResponse> getNotes(Long taskId, Long userId) {
        ImprovementTask task = requireTask(taskId);
        permissionService.assertCanViewVsmMap(userId, task.getKaizenNode().getVsmMap());
        return improvementTaskNoteRepository.findByImprovementTaskIdOrderByCreatedAtAsc(taskId).stream()
                .map(ImprovementTaskNoteResponse::from).toList();
    }

    /**
     * Lets a task's owner (or the map's author/admin) add a colleague from the same
     * department/scope as a collaborator: they can see the task and add notes, but {@link
     * #assertIsPullerOrAdmin} still gates every state transition, so they can never move it
     * themselves. Only valid while PULLED/IN_PROGRESS -- there's no "their task" yet in
     * BACKLOG/AVAILABLE, and a DONE task is already closed.
     */
    public ImprovementTaskAssignee addAssignee(Long taskId, Long employeeId, Long userId) {
        ImprovementTask task = requireTask(taskId);
        VsmMap map = task.getKaizenNode().getVsmMap();
        boolean isOwner = task.getPulledBy() != null && task.getPulledBy().getId().equals(userId);
        if (!isOwner && !permissionService.canEditVsmMap(userId, map)) {
            throw new UnauthorizedException("Only this task's owner or the map's author/admin can add a collaborator");
        }
        if (task.getState() != VsmTaskState.PULLED && task.getState() != VsmTaskState.IN_PROGRESS) {
            throw new BusinessRuleException("Only a pulled or in-progress task can have collaborators added");
        }
        if (task.getPulledBy() != null && task.getPulledBy().getId().equals(employeeId)) {
            throw new BusinessRuleException("This employee already owns the task");
        }
        if (improvementTaskAssigneeRepository.findByImprovementTaskIdAndEmployeeId(taskId, employeeId).isPresent()) {
            throw new BusinessRuleException("This employee is already a collaborator on this task");
        }
        AppUser employee = requireUser(employeeId);
        // Same visibility bound as pulling the task in the first place -- for a department-scoped
        // map, that means the employee must be in that department.
        permissionService.assertCanViewVsmMap(employeeId, map);
        AppUser addedBy = requireUser(userId);
        ImprovementTaskAssignee saved = improvementTaskAssigneeRepository.save(ImprovementTaskAssignee.builder()
                .improvementTask(task)
                .employee(employee)
                .addedBy(addedBy)
                .build());
        eventPublisher.publishEvent(new VsmTaskAssignedEvent(taskId, employeeId));
        return saved;
    }

    public void removeAssignee(Long taskId, Long employeeId, Long userId) {
        ImprovementTask task = requireTask(taskId);
        boolean isOwner = task.getPulledBy() != null && task.getPulledBy().getId().equals(userId);
        if (!isOwner && !permissionService.canEditVsmMap(userId, task.getKaizenNode().getVsmMap())) {
            throw new UnauthorizedException("Only this task's owner or the map's author/admin can remove a collaborator");
        }
        ImprovementTaskAssignee link = improvementTaskAssigneeRepository
                .findByImprovementTaskIdAndEmployeeId(taskId, employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("ImprovementTaskAssignee for this task and employee", employeeId));
        improvementTaskAssigneeRepository.delete(link);
    }

    /** Full detail for the task-progress page: real (not default-false) canEditMap/assignees/
     *  noteCount/canAddNote/canManageAssignees for this specific viewer. */
    public ImprovementTaskResponse getTaskDetail(Long taskId, Long userId) {
        ImprovementTask task = requireTask(taskId);
        permissionService.assertCanViewVsmMap(userId, task.getKaizenNode().getVsmMap());
        return toResponse(task, userId);
    }

    /** Every task this employee owns (pulled) or has been added to as a collaborator, across every
     *  map/department -- the "My Tasks" panel on the central dashboard, not scoped to one map. */
    public List<ImprovementTaskResponse> getMyTasks(Long userId) {
        Map<Long, ImprovementTask> byId = new LinkedHashMap<>();
        for (ImprovementTask task : improvementTaskRepository.findByPulledById(userId)) {
            byId.put(task.getId(), task);
        }
        List<Long> assignedTaskIds = improvementTaskAssigneeRepository.findByEmployeeId(userId).stream()
                .map(a -> a.getImprovementTask().getId()).toList();
        if (!assignedTaskIds.isEmpty()) {
            for (ImprovementTask task : improvementTaskRepository.findByIdIn(assignedTaskIds)) {
                byId.putIfAbsent(task.getId(), task);
            }
        }
        List<ImprovementTaskResponse> responses = new ArrayList<>();
        for (ImprovementTask task : byId.values()) {
            responses.add(toResponse(task, userId));
        }
        responses.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return responses;
    }

    /** Builds a fully-populated response for a single task and viewer -- used wherever the real
     *  (not default-false) canEditMap/assignees/noteCount/canAddNote/canManageAssignees are needed
     *  for exactly one task (the task-detail page, "my tasks"). Board *listing* endpoints bulk-fetch
     *  assignees/notes themselves (see VsmBoardService) rather than call this per row. */
    public ImprovementTaskResponse toResponse(ImprovementTask task, Long userId) {
        boolean canEditMap = permissionService.canEditVsmMap(userId, task.getKaizenNode().getVsmMap());
        List<ImprovementTaskAssignee> assignees = improvementTaskAssigneeRepository.findByImprovementTaskId(task.getId());
        long noteCount = improvementTaskNoteRepository.countByImprovementTaskId(task.getId());
        boolean isOwner = task.getPulledBy() != null && task.getPulledBy().getId().equals(userId);
        boolean canManageAssignees = canEditMap || isOwner;
        boolean isAssignee = assignees.stream().anyMatch(a -> a.getEmployee().getId().equals(userId));
        boolean canAddNote = canManageAssignees || isAssignee;
        return ImprovementTaskResponse.from(task, canEditMap, assignees, noteCount, canAddNote, canManageAssignees);
    }

    private boolean canAddNote(ImprovementTask task, AppUser author) {
        if (permissionService.canEditVsmMap(author.getId(), task.getKaizenNode().getVsmMap())) {
            return true;
        }
        if (task.getPulledBy() != null && task.getPulledBy().getId().equals(author.getId())) {
            return true;
        }
        return improvementTaskAssigneeRepository
                .findByImprovementTaskIdAndEmployeeId(task.getId(), author.getId()).isPresent();
    }

    /** Only the person who pulled this task, or an admin, may start/complete it or log its
     *  achievement -- exposed (not private) so VsmTaskAchievementService can share the same check. */
    public void assertIsPullerOrAdmin(ImprovementTask task, Long userId) {
        AppUser currentUser = requireUser(userId);
        if (currentUser.hasRole(SystemRole.ADMIN)) {
            return;
        }
        if (task.getPulledBy() == null || !task.getPulledBy().getId().equals(userId)) {
            throw new UnauthorizedException("Only the person who pulled this task or an admin can update it");
        }
    }

    private VsmNode requireNode(Long nodeId) {
        return vsmNodeRepository.findById(nodeId)
                .orElseThrow(() -> new ResourceNotFoundException("VsmNode", nodeId));
    }

    private ImprovementTask requireTask(Long taskId) {
        return improvementTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("ImprovementTask", taskId));
    }

    private AppUser requireUser(Long userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", userId));
    }
}
