package com.rit.spms.service;

import com.rit.spms.config.AiProperties;
import com.rit.spms.domain.AppUser;
import com.rit.spms.domain.enums.NotificationType;
import com.rit.spms.domain.enums.SystemRole;
import com.rit.spms.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Periodically probes whichever {@link AiEngineHealthChecker} is active for the configured
 * {@code app.ai.provider} and notifies every ADMIN when it stops responding, and again when it
 * recovers -- catching an AI-engine outage proactively instead of only surfacing it when an
 * employee happens to trigger a generation and hits {@link com.rit.spms.exception.AiUnavailableException}.
 *
 * <p>Provider-agnostic itself: it never talks to Ollama/Claude/etc. directly, only through the
 * pluggable {@link AiEngineHealthChecker}, so this class needs no changes when a new provider's
 * checker is added -- see that interface for what happens today when none is active (Claude).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiEngineHealthCheckService {

    private final Optional<AiEngineHealthChecker> healthChecker;
    private final AiProperties aiProperties;
    private final AppUserRepository appUserRepository;
    private final NotificationService notificationService;

    // Assume up at startup: the first real check either confirms that or flips it to down (and
    // notifies) on its own -- no need to notify preemptively before a single check has even run.
    private volatile boolean lastKnownUp = true;
    private volatile boolean warnedNoChecker = false;

    @Scheduled(fixedDelayString = "${app.ai.health-check.interval-ms:300000}")
    public void checkAiEngineHealth() {
        if (!aiProperties.getHealthCheck().isEnabled()) {
            return;
        }
        if (healthChecker.isEmpty()) {
            // Deliberately no fallback/no-op checker: reporting "up" for a provider we can't
            // actually probe would be worse than not monitoring at all (see AiEngineHealthChecker's
            // Javadoc). Log once at startup, not on every tick.
            if (!warnedNoChecker) {
                log.warn("No AiEngineHealthChecker implementation for app.ai.provider='{}' -- "
                        + "AI engine monitoring is inactive until one is added.", aiProperties.getProvider());
                warnedNoChecker = true;
            }
            return;
        }
        AiEngineHealthChecker checker = healthChecker.get();
        boolean up = checker.isResponding();
        if (up && !lastKnownUp) {
            log.info("AI engine ({}) is responding again", checker.providerName());
            notifyAdmins("The AI engine (" + checker.providerName() + ") is responding again.");
        } else if (!up && lastKnownUp) {
            log.warn("AI engine ({}) is not responding", checker.providerName());
            notifyAdmins("The AI engine (" + checker.providerName() + ") is not responding and needs attention.");
        }
        lastKnownUp = up;
    }

    private void notifyAdmins(String message) {
        List<AppUser> admins = appUserRepository.findByActiveTrueAndSystemRolesContaining(SystemRole.ADMIN);
        admins.forEach(admin -> notificationService.create(admin, message, NotificationType.AI_ENGINE_DOWN, null));
    }
}
