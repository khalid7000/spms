package com.rit.spms.domain.enums;

/** Kanban lifecycle of an {@link com.rit.spms.domain.ImprovementTask}. AVAILABLE/PULLED/IN_PROGRESS/
 *  DONE are the four board columns; BACKLOG is the leader's own draft state, before it's published
 *  to the board for faculty to see/pull. */
public enum VsmTaskState {
    BACKLOG,
    AVAILABLE,
    PULLED,
    IN_PROGRESS,
    DONE
}
