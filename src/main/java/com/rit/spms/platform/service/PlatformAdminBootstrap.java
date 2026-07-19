package com.rit.spms.platform.service;

import com.rit.spms.platform.domain.PlatformAdmin;
import com.rit.spms.platform.repository.PlatformAdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds exactly one Super Admin on first startup if {@code platform_admin} is empty --
 * every deployment (RIT's or a brand-new client's) needs some way to log in and create the
 * first organization, so unlike the retired tenant {@code DataInitializer}, this is
 * generic first-run bootstrap, not RIT-specific behavior, and is fine to run everywhere.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PlatformAdminBootstrap implements ApplicationRunner {

    private final PlatformAdminRepository platformAdminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.platform.bootstrap-admin-email:superadmin@platform.local}")
    private String bootstrapEmail;

    @Value("${app.platform.bootstrap-admin-password:changeme}")
    private String bootstrapPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (platformAdminRepository.count() > 0) {
            return;
        }

        PlatformAdmin admin = PlatformAdmin.builder()
                .email(bootstrapEmail)
                .passwordHash(passwordEncoder.encode(bootstrapPassword))
                .active(true)
                .build();
        platformAdminRepository.save(admin);

        log.warn("Seeded initial Super Admin account {} with the configured/default bootstrap "
                + "password -- change it immediately (app.platform.bootstrap-admin-email / "
                + "-password in application.yml control this on first run only).", bootstrapEmail);
    }
}
