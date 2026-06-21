package com.placement.portal.service.admin;

import com.placement.portal.domain.User;
import com.placement.portal.domain.enums.Role;
import com.placement.portal.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ensures a default ADMIN account exists on every startup.
 *
 * <p>Flyway is currently disabled in this project, so the V13 seed migration
 * never runs. This seeder fills that gap with the same credentials documented
 * in V13. It is idempotent — if a user with the admin email already exists,
 * nothing is written.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserSeeder {

    private static final String ADMIN_ID    = "a0000000-0000-0000-0000-000000000001";
    private static final String ADMIN_EMAIL = "admin@placementportal.edu";
    private static final String ADMIN_NAME  = "System Administrator";
    private static final String ADMIN_PASSWORD_PLAINTEXT = "Admin@1234";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    @Transactional
    public void seedIfMissing() {
        if (userRepository.findByEmail(ADMIN_EMAIL).isPresent()) {
            return;
        }
        User admin = User.builder()
                .id(ADMIN_ID)
                .email(ADMIN_EMAIL)
                .fullName(ADMIN_NAME)
                .passwordHash(passwordEncoder.encode(ADMIN_PASSWORD_PLAINTEXT))
                .role(Role.ADMIN)
                .isActive(true)
                .build();
        userRepository.save(admin);
        log.info("Seeded default ADMIN user with email {}", ADMIN_EMAIL);
    }
}
