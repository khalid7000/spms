package com.rit.spms.platform.tenant;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

/** Tells Hibernate which schema the current Session should use -- always whatever
 * {@link TenantContext} resolves to for this request thread. */
@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    @Override
    public String resolveCurrentTenantIdentifier() {
        return TenantContext.getTenant();
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return false;
    }
}
