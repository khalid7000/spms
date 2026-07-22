package com.rit.spms.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Settings for verifying sessions issued by a third-party SSO gateway that embeds this app
 * as a "module" under its own origin (e.g. RIT Dubai's Academic Dashboard) -- see
 * {@code docs/Academic-Dashboard-Module-Integration-Contract.md} for the normative spec this
 * class implements against. Set gateway-sso.enabled=true in application.yml (or an
 * environment-specific override) to activate at deployment time -- no code changes required,
 * same convention as {@link LdapProperties}. Entirely independent of this app's own
 * HS256-signed session tokens ({@code JwtTokenProvider}) and of the LDAP toggle above -- a
 * deployment can have any combination of local/LDAP/gateway-SSO active at once, since each is
 * just an additional way to arrive at a valid {@code UserPrincipal}.
 */
@Component
@ConfigurationProperties(prefix = "gateway-sso")
@Getter
@Setter
public class GatewaySsoProperties {

    /** Set to true to accept the gateway's JWT (cookie or Bearer) as an alternative session. */
    private boolean enabled = false;

    /** JWKS endpoint for RS256 signature verification, e.g.
     * https://ie.ritdubai.ae/.well-known/jwks.json -- confirm the exact URL with the
     * gateway's maintainers before enabling in a real deployment. */
    private String jwksUri = "";

    /** Expected JWT `iss` claim -- confirm the production value with maintainers; their own
     * docs show "academicspring-auth" only as a local-dev example, not a guaranteed value. */
    private String issuer = "";

    /** Name of the HttpOnly session cookie the gateway sets, read as a fallback when no
     * Authorization: Bearer header is present. */
    private String cookieName = "rit_session";

    /** POST endpoint the gateway exposes to check whether a token's `jti` has been revoked
     * (logged out) since issuance, e.g. https://ie.ritdubai.ae/auth/introspect. Required
     * unless the gateway's maintainers approve a bounded cache interval instead (see
     * revocationCacheSeconds below) -- signature validity alone does not reflect logout. */
    private String introspectUri = "";

    /** 0 (default) calls introspectUri on every protected request, matching the gateway's
     * contract default. Only set this above 0 if the gateway's maintainers have explicitly
     * approved a bounded cache interval for this deployment -- record the approved value in
     * the module's handoff package, not just here. */
    private int revocationCacheSeconds = 0;

    /** Clock skew tolerance for `exp`/`iat` validation, matching normal JWT library defaults. */
    private int clockSkewSeconds = 60;

    /** Generic knob for any gateway-SSO deployment whose gateway issues `email` under more
     * than one domain for the same person while this app stores each user under exactly one.
     * Leave blank (default) to trust the JWT's `email` claim as-is -- safe whenever the
     * gateway issues a single, stable domain, which is the norm.
     *
     * <p><b>RIT Dubai specifically:</b> confirmed their Academic Dashboard Gateway issues
     * {@code uid@rit.edu} and {@code uid@g.rit.edu} interchangeably for the same person, while
     * every existing StratAlign AppUser row for RIT is stored under {@code @rit.edu} only. Set
     * this to {@code "rit.edu"} for the RIT deployment specifically so the filter reconstructs
     * {@code uid + "@rit.edu"} from the JWT's `uid` claim instead of trusting whichever domain
     * a given token happens to carry. This dual-domain behavior is RIT's own gateway's
     * particularity, confirmed for that deployment only -- do not assume it generalizes to any
     * other gateway-SSO integration; leave this blank elsewhere unless independently confirmed. */
    private String canonicalEmailDomain = "";
}
