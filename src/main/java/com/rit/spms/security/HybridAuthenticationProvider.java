package com.rit.spms.security;

import com.rit.spms.config.LdapProperties;
import com.rit.spms.domain.AppUser;
import com.rit.spms.repository.AppUserRepository;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Delegates to LDAP when {@code ldap.enabled=true}, otherwise verifies passwords
 * against the local BCrypt hash.  Switching modes requires only a config change.
 */
@Component
public class HybridAuthenticationProvider implements AuthenticationProvider {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LdapProperties ldapProperties;
    private final LdapAuthenticator ldapAuthenticator; // null when LDAP disabled

    public HybridAuthenticationProvider(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            LdapProperties ldapProperties,
            Optional<LdapAuthenticator> ldapAuthenticator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.ldapProperties = ldapProperties;
        this.ldapAuthenticator = ldapAuthenticator.orElse(null);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName().toLowerCase().trim();
        String password = (String) authentication.getCredentials();

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No account found: " + email));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new DisabledException("Account is inactive");
        }

        if (ldapProperties.isEnabled()) {
            verifyLdap(email, password);
        } else {
            verifyLocal(password, user.getPasswordHash());
        }

        return new UsernamePasswordAuthenticationToken(
                UserPrincipal.from(user), null, UserPrincipal.from(user).getAuthorities());
    }

    private void verifyLdap(String email, String password) {
        if (ldapAuthenticator == null) {
            throw new IllegalStateException(
                    "ldap.enabled=true but no LdapAuthenticator bean is available");
        }
        try {
            ldapAuthenticator.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));
        } catch (Exception ex) {
            throw new BadCredentialsException("Invalid credentials", ex);
        }
    }

    private void verifyLocal(String rawPassword, String storedHash) {
        if (storedHash == null || !passwordEncoder.matches(rawPassword, storedHash)) {
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
