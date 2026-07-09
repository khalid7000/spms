package com.rit.spms.config;

import com.rit.spms.domain.AppUser;
import com.rit.spms.domain.enums.SystemRole;
import com.rit.spms.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Seeds the default system administrator on first startup.
 * Password defaults to the email address; user is forced to change it on first login.
 * In LDAP mode the password field is unused, but the account still needs to exist locally.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private static final String ADMIN_EMAIL = "admin@rit.edu";

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LdapProperties ldapProperties;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail(ADMIN_EMAIL)) {
            return;
        }

        // Default password = email; forced change on first login (skipped in LDAP mode)
        String passwordHash = passwordEncoder.encode(ADMIN_EMAIL);
        boolean forceChange = !ldapProperties.isEnabled();

        AppUser admin = AppUser.builder()
                .fname("System")
                .lname("Admin")
                .email(ADMIN_EMAIL)
                .passwordHash(passwordHash)
                .systemRoles(Set.of(SystemRole.ADMIN))
                .active(true)
                .mustChangePassword(forceChange)
                .build();

        userRepository.save(admin);
        log.info("Created default admin account: {} (mustChangePassword={})", ADMIN_EMAIL, forceChange);
    }
}
