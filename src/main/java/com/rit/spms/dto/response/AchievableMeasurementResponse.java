package com.rit.spms.dto.response;

import lombok.Builder;
import lombok.Data;

/** One Measurement a user can log an achievement against -- their own DEPLOYED strategies'
 *  Initiatives, flattened. Backs the Initiative/Measurement picker in the "log achievement to
 *  complete an IMPROVEMENT task" flow (see VsmTaskAchievementService), since no such picker existed
 *  anywhere else in the app before Phase 4 (every prior achievement-logging entry point already had
 *  a specific measurement in context from the Strategy Tree). */
@Data
@Builder
public class AchievableMeasurementResponse {
    private Long measurementId;
    private String measurementDescription;
    private Long initiativeId;
    private String initiativeTitle;
    private Long strategyId;
    private String strategyTitle;

    /** Base (not-yet-year-copied) initiatives, the common case, have no assessment period of their
     *  own -- see VsmTaskAchievementService's javadoc. The frontend fetches this planning cycle's
     *  periods (the same endpoint the Strategy Tree's own period filter uses) and has the completer
     *  pick one explicitly, rather than guessing one. */
    private Long planningCycleId;
}
