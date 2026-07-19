package com.rit.spms.platform.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.io.IOException;
import java.util.Map;

/**
 * Resolves {@link TenantContext} from the URL for the one pre-auth endpoint that needs it --
 * {@code POST /api/auth/{slug}/login} -- and must run before Spring's Open-Session-In-View
 * filter opens this request's one shared Hibernate session.
 *
 * <p>This isn't optional ordering: OSIV resolves and FIXES a session's tenant identifier
 * once, at the moment the session is created (at the very start of request processing, before
 * any controller code runs), and reuses that same session/connection for the rest of the
 * request regardless of anything set afterward. Setting {@link TenantContext} inside
 * {@code AuthController.loginToOrganization} was verified (against a real provisioning +
 * login run) to be too late for exactly this reason -- the authentication query still landed
 * against the default schema. Registered ahead of {@code JwtAuthenticationFilter} in
 * {@code SecurityConfig}, which itself runs ahead of OSIV the same way (that's why
 * already-authenticated tenant requests resolve correctly today).
 *
 * <p>Uses plain JDBC against platform.organization, never JPA -- the entire point is to
 * resolve the tenant BEFORE any Hibernate session for this request exists.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TenantResolutionFilter extends OncePerRequestFilter {

    private static final PathPattern SLUG_LOGIN_PATTERN =
            PathPatternParser.defaultInstance.parse("/api/auth/{slug}/login");

    private final JdbcTemplate jdbcTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        var matchInfo = SLUG_LOGIN_PATTERN.matchAndExtract(
                org.springframework.http.server.PathContainer.parsePath(request.getRequestURI()));

        if (matchInfo != null) {
            String slug = matchInfo.getUriVariables().get("slug");
            try {
                Map<String, Object> row = jdbcTemplate.queryForMap(
                        "SELECT schema_name FROM platform.organization WHERE slug = ? AND status = 'ACTIVE'", slug);
                TenantContext.setTenant((String) row.get("schema_name"));
            } catch (EmptyResultDataAccessException e) {
                log.debug("No active organization for slug '{}' -- leaving default tenant, "
                        + "the controller will 404/reject it properly", slug);
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
