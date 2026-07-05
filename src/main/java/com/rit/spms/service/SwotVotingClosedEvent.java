package com.rit.spms.service;

/**
 * Published by {@link SwotVotingService} once every participant has voted and results are
 * tallied. {@link SwotSuggestionService} listens for this AFTER_COMMIT (see its
 * onVotingClosed) rather than being called directly from within the vote-tally transaction —
 * calling straight through would race the still-uncommitted tally: the background AI-generation
 * thread could start before the just-saved SwotQuadrantResult rows are actually visible to it.
 */
public record SwotVotingClosedEvent(Long strategyId) {
}
