package com.rit.spms.domain.enums;

/**
 * Lifecycle of a {@link com.rit.spms.domain.VsmMap}. Deliberately simpler than {@link StrategyState}
 * -- a Value Stream Map is a standalone, ongoing artifact (not tied to a yearly planning cycle), so
 * there is no approval-gated deploy step, just draft/active/archived.
 */
public enum VsmMapState {
    /** Still being built (manually or from an AI draft) -- Kaizen-burst nodes can't spawn tasks yet. */
    DRAFT,
    /** Published; Kaizen-burst nodes on this map can spawn improvement tasks (see Phase 3). */
    ACTIVE,
    /** Retired, read-only. */
    ARCHIVED
}
