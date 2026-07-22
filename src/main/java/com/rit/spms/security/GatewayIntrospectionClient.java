package com.rit.spms.security;

import com.rit.spms.config.GatewaySsoProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Checks whether a gateway-issued JWT's {@code jti} has been revoked (logged out) since
 * issuance -- signature validity alone doesn't reflect logout, per the integration contract's
 * ยง6.5. Calls the gateway's {@code POST /auth/introspect} on every check by default
 * ({@code gateway-sso.revocation-cache-seconds=0}), matching the contract's default; only
 * caches locally if a deployment has an explicitly maintainer-approved bounded interval
 * configured (see {@link GatewaySsoProperties#getRevocationCacheSeconds()}).
 *
 * <p>Fails closed: any error reaching the introspection endpoint is treated as revoked,
 * per the module rules' "Fail closed" requirement -- a gateway outage should not silently
 * leave stale sessions valid.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GatewayIntrospectionClient {

    private final GatewaySsoProperties properties;
    private final Map<String, CachedResult> cache = new ConcurrentHashMap<>();

    private record CachedResult(boolean revoked, Instant expiresAt) {
    }

    public boolean isRevoked(String jti) {
        int cacheSeconds = properties.getRevocationCacheSeconds();
        if (cacheSeconds > 0) {
            CachedResult cached = cache.get(jti);
            if (cached != null && cached.expiresAt().isAfter(Instant.now())) {
                return cached.revoked();
            }
        }

        boolean revoked = checkWithGateway(jti);
        if (cacheSeconds > 0) {
            cache.put(jti, new CachedResult(revoked, Instant.now().plusSeconds(cacheSeconds)));
        }
        return revoked;
    }

    private boolean checkWithGateway(String jti) {
        try {
            IntrospectResponse response = RestClient.create()
                    .post()
                    .uri(properties.getIntrospectUri())
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(Map.of("jti", jti))
                    .retrieve()
                    .body(IntrospectResponse.class);
            return response == null || response.revoked();
        } catch (Exception ex) {
            log.warn("Gateway introspection call failed for jti={}; failing closed (treating as revoked)",
                    jti, ex);
            return true;
        }
    }

    private record IntrospectResponse(String jti, boolean revoked) {
    }
}
