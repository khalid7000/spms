package com.rit.spms.service;

import java.math.BigDecimal;

/**
 * Pluggable AI provider for proposing a measurement (KPI) for a not-yet-saved Initiative,
 * given its parent Objective's text. Selected at runtime via {@code app.ai.provider}
 * (application.yml) -- same mechanism as {@link SwotAreaGoalGenerator} and
 * {@link PortfolioGoalSuggestionGenerator}. Unlike those two, this call is made
 * synchronously while the user waits in the Add Initiative modal (the prompt/response are
 * small enough that this is workable -- see {@link OllamaMeasurementSuggestionGenerator}'s
 * dedicated short timeout), not as a background job with a poll/review step.
 */
public interface MeasurementSuggestionGenerator {

    record SuggestedMeasurementDto(String description, String unit, BigDecimal targetValue) {
    }

    SuggestedMeasurementDto suggestMeasurement(String objectiveTitle, String objectiveDescription,
                                                String initiativeTitle, String initiativeDescription);

    /** Recorded nowhere today (this suggestion isn't persisted) -- kept for parity with the
     * other AI provider interfaces in case an audit trail is added later. */
    String providerName();
}
