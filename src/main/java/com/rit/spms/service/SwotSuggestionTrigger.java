package com.rit.spms.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Bridges {@link SwotVotingClosedEvent} to {@link SwotSuggestionService#generateSuggestions}.
 *
 * This has to be a separate bean, not just a method on SwotSuggestionService itself: Spring's
 * @Async only applies to a call that goes through the bean's proxy, and a listener method calling
 * a sibling method on the *same* class (self-invocation) bypasses that proxy entirely — the
 * generation would then run synchronously on the request thread that closed the vote, blocking it
 * for as long as the model call takes. Routing through this separate bean's injected reference
 * makes it a genuine external call, so @Async actually takes effect.
 */
@Component
@RequiredArgsConstructor
public class SwotSuggestionTrigger {

    private final SwotSuggestionService swotSuggestionService;

    /**
     * AFTER_COMMIT matters here too: this fires once SwotVotingService's vote-tally transaction has
     * actually committed, so the background generation thread it kicks off is guaranteed to see the
     * tally that triggered it.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onVotingClosed(SwotVotingClosedEvent event) {
        swotSuggestionService.generateSuggestions(event.strategyId());
    }
}
