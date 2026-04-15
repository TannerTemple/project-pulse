package edu.tcu.cs.projectpulse;

import edu.tcu.cs.projectpulse.user.AppUser;
import edu.tcu.cs.projectpulse.user.UserRepository;
import edu.tcu.cs.projectpulse.user.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds a default Admin account on first startup if none exists.
 *
 * Dev credentials:
 *   Email:    admin@projectpulse.app
 *   Password: Admin1234!
 *
 * Change the password immediately after the first login in any non-local environment.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        boolean adminExists = userRepository.findByRole(UserRole.ADMIN)
                .stream().findAny().isPresent();

        if (!adminExists) {
            AppUser admin = new AppUser();
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setEmail("admin@projectpulse.app");
            admin.setPasswordHash(passwordEncoder.encode("Admin1234!"));
            admin.setRole(UserRole.ADMIN);
            admin.setActive(true);
            admin.setRegistrationComplete(true);
            userRepository.save(admin);
            log.info("==========================================================");
            log.info("  Default admin seeded.");
            log.info("  Email:    admin@projectpulse.app");
            log.info("  Password: Admin1234!");
            log.info("  Change this password before deploying to production.");
            log.info("==========================================================");
        }
    }
}
