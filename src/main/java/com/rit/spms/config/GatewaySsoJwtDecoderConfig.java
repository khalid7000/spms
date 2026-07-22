package com.rit.spms.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.time.Duration;
import java.util.List;

/**
 * Builds the {@link JwtDecoder} that verifies the gateway's RS256-signed session tokens via
 * its published JWKS. Only constructed when {@code gateway-sso.enabled=true} -- otherwise no
 * JWKS fetch is ever attempted, so a deployment with the feature off (every deployment except
 * RIT's, today) pays zero cost and needs zero network reachability to any gateway.
 *
 * <p>{@link NimbusJwtDecoder#withJwkSetUri} already handles JWKS fetching/caching and
 * re-fetch-on-unknown-{@code kid} internally -- no hand-rolled caching needed here, matching
 * the integration contract's "cache <= 1 hour; refresh on unknown kid" requirement.
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "gateway-sso", name = "enabled", havingValue = "true")
public class GatewaySsoJwtDecoderConfig {

    private final GatewaySsoProperties properties;

    @Bean
    public JwtDecoder gatewaySsoJwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(properties.getJwksUri()).build();

        OAuth2TokenValidator<Jwt> withClockSkew =
                new JwtTimestampValidator(Duration.ofSeconds(properties.getClockSkewSeconds()));
        OAuth2TokenValidator<Jwt> withIssuer = new JwtIssuerValidator(properties.getIssuer());
        OAuth2TokenValidator<Jwt> requireJti = new RequireJtiValidator();

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                List.of(JwtValidators.createDefault(), withClockSkew, withIssuer, requireJti)));
        return decoder;
    }

    /** The gateway's contract requires a non-empty `jti` on every token (it's their revocation
     * key) -- Spring's default validators don't check for claim presence beyond exp/nbf, so
     * this closes that gap explicitly rather than discovering a missing jti only when the
     * later introspection call NPEs. */
    private static class RequireJtiValidator implements OAuth2TokenValidator<Jwt> {
        @Override
        public OAuth2TokenValidatorResult validate(Jwt token) {
            String jti = token.getId();
            if (jti == null || jti.isBlank()) {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("missing_jti", "Token has no jti claim", null));
            }
            return OAuth2TokenValidatorResult.success();
        }
    }
}
