package com.rit.spms.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rit.spms.config.GatewaySsoProperties;
import com.rit.spms.domain.AppUser;
import com.rit.spms.domain.enums.NotificationType;
import com.rit.spms.domain.enums.SystemRole;
import com.rit.spms.repository.AppUserRepository;
import com.rit.spms.service.NotificationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Accepts a gateway-issued RS256 JWT (e.g. RIT Dubai's Academic Dashboard Gateway) as an
 * alternative to this app's own session, per the integration contract. A no-op when
 * {@code gateway-sso.enabled=false} (every deployment except RIT's, today) or when the
 * request carries no gateway token at all -- in either case the request simply falls through
 * to whatever {@link JwtAuthenticationFilter} or the unauthenticated path already does, same
 * "swallow and continue" discipline that filter itself uses. Registered alongside, not instead
 * of, that filter: whichever token type is actually present on a given request is the one
 * that ends up setting the {@code SecurityContext}.
 *
 * <p>Deliberately does not go through an {@code AuthenticationProvider} -- exactly like
 * {@link JwtAuthenticationFilter}, it sets the {@code SecurityContext} directly, since
 * {@code HybridAuthenticationProvider} is only wired for the username/password login path.
 *
 * <p>HTTP semantics follow the contract precisely: an invalid/expired/revoked token is left
 * unauthenticated (letting {@code SecurityConfig}'s {@code anyRequest().authenticated()}
 * produce the standard 401), while a *valid* token for a user this app doesn't recognize gets
 * an explicit 403 written here -- "authenticated but not provisioned" is a distinct case the
 * contract requires, and Spring's default unauthenticated-request handling can't express it.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GatewaySsoAuthenticationFilter extends OncePerRequestFilter {

    /** Don't re-notify Admins for the same unrecognized email more than once per window --
     * a gateway-authenticated but unprovisioned visitor's browser will retry several API calls
     * per page load, and each would otherwise fire its own round of Admin notifications. */
    private static final long RENOTIFY_WINDOW_MINUTES = 30;

    private final GatewaySsoProperties properties;
    private final Optional<JwtDecoder> gatewaySsoJwtDecoder;
    private final GatewayIntrospectionClient introspectionClient;
    private final AppUserRepository appUserRepository;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final Map<String, Instant> lastNotifiedAt = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!properties.isEnabled() || gatewaySsoJwtDecoder.isEmpty()
                || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Jwt jwt = gatewaySsoJwtDecoder.get().decode(token);
            String jti = jwt.getId();
            if (introspectionClient.isRevoked(jti)) {
                log.debug("Gateway JWT jti={} is revoked; leaving request unauthenticated", jti);
                filterChain.doFilter(request, response);
                return;
            }

            String email = resolveCanonicalEmail(jwt);
            if (!StringUtils.hasText(email)) {
                log.warn("Gateway JWT has neither a usable uid nor email claim; cannot provision a StratAlign session");
                filterChain.doFilter(request, response);
                return;
            }

            Optional<AppUser> user = appUserRepository.findByEmail(email);
            if (user.isEmpty() || !Boolean.TRUE.equals(user.get().getActive())) {
                notifyAdminsOfUnprovisionedAccess(email, jwt, request);
                writeForbidden(response,
                        "Your account isn't set up in StratAlign yet. Please contact your administrator.");
                return;
            }

            UserPrincipal principal = UserPrincipal.from(user.get());
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException ex) {
            log.debug("Gateway JWT rejected: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /** Prefers the JWT's `uid` claim (falling back to `sub`, per the integration contract
     * ยง5.2) reconstructed against this deployment's canonical email domain -- RIT Dubai's
     * Gateway issues `email` under either "rit.edu" or "g.rit.edu" for the same person, but
     * every StratAlign AppUser row is always stored under exactly one. Falls back to the raw
     * `email` claim when no canonical domain is configured. */
    private String resolveCanonicalEmail(Jwt jwt) {
        String domain = properties.getCanonicalEmailDomain();
        if (StringUtils.hasText(domain)) {
            String uid = jwt.getClaimAsString("uid");
            if (!StringUtils.hasText(uid)) {
                uid = jwt.getSubject();
            }
            return StringUtils.hasText(uid) ? (uid.trim().toLowerCase() + "@" + domain.trim().toLowerCase()) : null;
        }
        String email = jwt.getClaimAsString("email");
        return StringUtils.hasText(email) ? email.toLowerCase().trim() : null;
    }

    /** A gateway-verified identity with no matching StratAlign account is worth surfacing to
     * every Admin -- it's either a real new hire/student who needs provisioning, or, since the
     * gateway already vouched for the identity, at minimum a signal worth a human looking at.
     * Deduped per email within {@link #RENOTIFY_WINDOW_MINUTES} so repeated calls from the same
     * unprovisioned visitor's page load don't flood every Admin's inbox. */
    private void notifyAdminsOfUnprovisionedAccess(String email, Jwt jwt, HttpServletRequest request) {
        Instant now = Instant.now();
        Instant last = lastNotifiedAt.get(email);
        if (last != null && last.plusSeconds(RENOTIFY_WINDOW_MINUTES * 60).isAfter(now)) {
            return;
        }
        lastNotifiedAt.put(email, now);

        String givenName = jwt.getClaimAsString("given_name");
        String familyName = jwt.getClaimAsString("family_name");
        String name = StringUtils.hasText(givenName) || StringUtils.hasText(familyName)
                ? ((givenName == null ? "" : givenName) + " " + (familyName == null ? "" : familyName)).trim()
                : "unknown";

        String message = "An RIT-authenticated user tried to access StratAlign but has no account here. "
                + "Email: " + email + ". Name: " + name + ". IP: " + request.getRemoteAddr()
                + ". Time: " + now + ".";

        List<AppUser> admins = appUserRepository.findByActiveTrueAndSystemRolesContaining(SystemRole.ADMIN);
        admins.forEach(admin -> notificationService.create(admin, message, NotificationType.GATEWAY_SSO_UNPROVISIONED, null));
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (properties.getCookieName().equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void writeForbidden(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(),
                Map.of("success", false, "message", message, "code", "MODULE_USER_NOT_FOUND"));
    }
}
