package com.rit.spms.platform.tenant;

/**
 * Holds the Postgres schema the current request thread should be scoped to. Populated by
 * {@code JwtAuthenticationFilter} from the JWT's signed "org" claim on authenticated
 * requests -- never from a URL segment, so a token minted for one org can't be pointed at
 * another org's schema by editing the address bar. Defaults to {@link #DEFAULT_SCHEMA}
 * (today's implicit single schema) when nothing sets it, which is what keeps every
 * existing request behaviorally unchanged until an organization is actually registered
 * and its users start receiving tokens with a real "org" claim.
 */
public final class TenantContext {

    /** The schema every request used implicitly before multi-tenancy existed. */
    public static final String DEFAULT_SCHEMA = "public";

    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setTenant(String schemaName) {
        CURRENT.set(schemaName);
    }

    public static String getTenant() {
        String schema = CURRENT.get();
        return schema != null ? schema : DEFAULT_SCHEMA;
    }

    /** Tomcat reuses request-handling threads -- this must be called in a `finally` around
     * every filter chain that calls {@link #setTenant}, or one request's tenant leaks into
     * the next request handled by the same thread. */
    public static void clear() {
        CURRENT.remove();
    }
}
