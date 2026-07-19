package com.rit.spms.platform.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Second, independent {@link SecurityFilterChain} scoped to {@code /api/platform/**} --
 * ordered ahead of the main tenant chain in {@code SecurityConfig} so it claims those paths
 * first. Deliberately has no {@code authenticationProvider}/{@code HybridAuthenticationProvider}
 * wiring at all: Super Admin auth never touches per-org LDAP logic.
 */
@Configuration
@RequiredArgsConstructor
public class PlatformSecurityConfig {

    private final PlatformJwtAuthenticationFilter platformJwtAuthenticationFilter;
    // Reuses SecurityConfig's own CorsConfigurationSource bean (same allowed origins/methods) --
    // a second, independent SecurityFilterChain still needs its OWN .cors(...) call, or the
    // browser's preflight for /api/platform/** gets no Access-Control-Allow-Origin header at
    // all and every request from the console's frontend fails before it's even sent.
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    @Order(1)
    public SecurityFilterChain platformFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/platform/**")
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/platform/auth/**").permitAll()
                .anyRequest().hasRole("PLATFORM_ADMIN")
            )
            .addFilterBefore(platformJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
