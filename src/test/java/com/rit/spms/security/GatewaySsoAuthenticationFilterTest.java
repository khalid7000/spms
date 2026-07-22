package com.rit.spms.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rit.spms.config.GatewaySsoProperties;
import com.rit.spms.domain.AppUser;
import com.rit.spms.domain.enums.NotificationType;
import com.rit.spms.domain.enums.SystemRole;
import com.rit.spms.repository.AppUserRepository;
import com.rit.spms.service.NotificationService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Covers the filter's own concerns from the integration contract's ยง6.9 matrix: revoked jti
 * and unprovisioned-user (403). Decoder-level concerns (expiry, wrong issuer, unknown kid) are
 * covered in {@code GatewaySsoJwtDecoderConfigTest} instead, using a mocked {@link JwtDecoder}
 * here so this test stays focused on what the filter itself decides once a token is already
 * known to be validly signed.
 */
class GatewaySsoAuthenticationFilterTest {

    private GatewaySsoProperties properties;
    private JwtDecoder jwtDecoder;
    private GatewayIntrospectionClient introspectionClient;
    private AppUserRepository appUserRepository;
    private NotificationService notificationService;
    private GatewaySsoAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        properties = new GatewaySsoProperties();
        properties.setEnabled(true);
        jwtDecoder = mock(JwtDecoder.class);
        introspectionClient = mock(GatewayIntrospectionClient.class);
        appUserRepository = mock(AppUserRepository.class);
        notificationService = mock(NotificationService.class);
        filter = new GatewaySsoAuthenticationFilter(properties, Optional.of(jwtDecoder), introspectionClient,
                appUserRepository, notificationService, new ObjectMapper());
    }

    private Jwt validJwt(String email) {
        return Jwt.withTokenValue("token-value")
                .header("alg", "RS256")
                .claim("email", email)
                .claim("uid", "abc1234")
                .jti("jti-" + email)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
    }

    @Test
    void disabledFeature_leavesRequestUnauthenticated() throws Exception {
        properties.setEnabled(false);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer irrelevant");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(jwtDecoder);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void revokedJti_leavesRequestUnauthenticated() throws Exception {
        Jwt jwt = validJwt("faculty@g.rit.edu");
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
        when(introspectionClient.isRevoked(jwt.getId())).thenReturn(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer some-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(response.getStatus()).isEqualTo(200); // filter doesn't write a response; 401 comes from authorizeHttpRequests downstream
    }

    @Test
    void unprovisionedUser_returns403AndNotifiesAdmins() throws Exception {
        Jwt jwt = validJwt("stranger@g.rit.edu");
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
        when(introspectionClient.isRevoked(jwt.getId())).thenReturn(false);
        when(appUserRepository.findByEmail("stranger@g.rit.edu")).thenReturn(Optional.empty());

        AppUser admin = AppUser.builder().id(1L).fname("Admin").lname("User")
                .email("admin@strtalign.com").active(true).build();
        when(appUserRepository.findByActiveTrueAndSystemRolesContaining(SystemRole.ADMIN))
                .thenReturn(List.of(admin));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer some-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain, never()).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("MODULE_USER_NOT_FOUND");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        verify(notificationService).create(eq(admin), contains("stranger@g.rit.edu"),
                eq(NotificationType.GATEWAY_SSO_UNPROVISIONED), isNull());
    }

    @Test
    void unprovisionedUser_doesNotReNotifyWithinDedupWindow() throws Exception {
        Jwt jwt = validJwt("stranger@g.rit.edu");
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
        when(introspectionClient.isRevoked(jwt.getId())).thenReturn(false);
        when(appUserRepository.findByEmail("stranger@g.rit.edu")).thenReturn(Optional.empty());
        AppUser admin = AppUser.builder().id(1L).fname("Admin").lname("User")
                .email("admin@strtalign.com").active(true).build();
        when(appUserRepository.findByActiveTrueAndSystemRolesContaining(SystemRole.ADMIN))
                .thenReturn(List.of(admin));

        for (int i = 0; i < 3; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer some-token");
            filter.doFilterInternal(request, new MockHttpServletResponse(), mock(FilterChain.class));
        }

        verify(notificationService, times(1)).create(any(), anyString(), any(), any());
    }

    @Test
    void canonicalEmailDomain_normalizesGatewayEmailForLookup() throws Exception {
        // RIT Dubai's Gateway may issue either domain for the same person; StratAlign always
        // stores @rit.edu -- this config is RIT-specific, not a default (see GatewaySsoProperties).
        properties.setCanonicalEmailDomain("rit.edu");
        Jwt jwt = validJwt("abc1234@g.rit.edu"); // gateway sends the g.rit.edu variant
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
        when(introspectionClient.isRevoked(jwt.getId())).thenReturn(false);

        AppUser user = AppUser.builder().id(9L).fname("A").lname("B")
                .email("abc1234@rit.edu").active(true).build(); // stored under rit.edu
        when(appUserRepository.findByEmail("abc1234@rit.edu")).thenReturn(Optional.of(user));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer some-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, mock(FilterChain.class));

        verify(appUserRepository).findByEmail("abc1234@rit.edu");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    void validTokenAndProvisionedUser_setsSecurityContext() throws Exception {
        Jwt jwt = validJwt("faculty@g.rit.edu");
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
        when(introspectionClient.isRevoked(jwt.getId())).thenReturn(false);

        AppUser user = AppUser.builder()
                .id(42L).fname("Ada").lname("Lovelace")
                .email("faculty@g.rit.edu").active(true)
                .build();
        when(appUserRepository.findByEmail("faculty@g.rit.edu")).thenReturn(Optional.of(user));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer some-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assertThat(principal.getEmail()).isEqualTo("faculty@g.rit.edu");
    }

    @Test
    void invalidSignature_leavesRequestUnauthenticated() throws Exception {
        when(jwtDecoder.decode(anyString())).thenThrow(new JwtException("bad signature"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer some-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void readsTokenFromGatewayCookie_whenNoBearerHeaderPresent() throws Exception {
        Jwt jwt = validJwt("faculty@g.rit.edu");
        when(jwtDecoder.decode("cookie-token-value")).thenReturn(jwt);
        when(introspectionClient.isRevoked(jwt.getId())).thenReturn(false);
        AppUser user = AppUser.builder().id(1L).fname("A").lname("B").email("faculty@g.rit.edu").active(true).build();
        when(appUserRepository.findByEmail("faculty@g.rit.edu")).thenReturn(Optional.of(user));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new jakarta.servlet.http.Cookie("rit_session", "cookie-token-value"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }
}
