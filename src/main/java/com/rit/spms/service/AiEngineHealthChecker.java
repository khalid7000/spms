package com.rit.spms.service;

/**
 * Pluggable "is the configured AI engine up and responding" probe -- one implementation per
 * {@code app.ai.provider} value, mirroring the {@link SwotAreaGoalGenerator}/
 * {@link TeachingEvaluationDraftGenerator} pattern elsewhere in this codebase.
 * {@link AiEngineHealthCheckService} owns the actual scheduling/admin-notification logic and is
 * provider-agnostic; it just calls whichever implementation is active for the configured provider.
 *
 * <p>Only {@link OllamaEngineHealthChecker} exists today. There is deliberately no Claude
 * implementation yet: Ollama is a local process that can crash or hang, so "is it up" is a
 * meaningful process-liveness question; Claude is a hosted API that essentially doesn't "go down"
 * the same way -- the meaningful check there is closer to "is our API key valid and is the network
 * path to Anthropic reachable," which is a different check to design deliberately, not a drop-in
 * copy of the Ollama probe. If {@code app.ai.provider} is switched to {@code claude}, no bean
 * implements this interface, and {@link AiEngineHealthCheckService} logs that once at startup and
 * stops running -- it does NOT silently report "up" for a provider it can't actually check, which
 * would be worse than not monitoring at all. Add a {@code ClaudeEngineHealthChecker} implementing
 * this interface (see {@link OllamaEngineHealthChecker} for the shape) to restore monitoring under
 * Claude.
 */
public interface AiEngineHealthChecker {
    boolean isResponding();

    String providerName();
}
