package com.rit.spms.domain.enums;

/** Leader-assigned severity for an {@link com.rit.spms.domain.ImprovementTask}, set at creation.
 *  Only IMPROVEMENT tasks will require the full achievement-linkage flow to close (Phase 4) --
 *  MINOR tasks close directly, since not every fix off a Kaizen burst rises to the level of a real
 *  achievement. */
public enum VsmTaskType {
    MINOR,
    IMPROVEMENT
}
