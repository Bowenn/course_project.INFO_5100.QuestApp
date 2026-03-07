package edu.info5100.questapp.config;

import edu.info5100.questapp.user.Role;
import edu.info5100.questapp.user.User;
import edu.info5100.questapp.user.UserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Initializes required system data on startup.
 * - Always ensures at least one ADMIN account exists.
 * - Seeds dev test USER accounts if no regular users exist yet.
 */
@Configuration
public class DataLoader {

    @Bean
    public ApplicationRunner loadData(UserRepository userRepository, PasswordEncoder encoder) {
        return args -> {
            // Always ensure an admin exists — ADMIN accounts are not creatable via registration
            if (userRepository.findByRole(Role.ADMIN).isEmpty()) {
                userRepository.save(new User(
                    "admin",
                    "admin@questapp.com",
                    encoder.encode("admin123"),
                    Role.ADMIN
                ));
            }

            // Seed dev test accounts if no regular users exist yet
            if (userRepository.findByRole(Role.USER).isEmpty()) {
                userRepository.save(new User("alice", "alice@test.com", encoder.encode("password"), Role.USER));
                userRepository.save(new User("bob", "bob@test.com", encoder.encode("password"), Role.USER));
            }
        };
    }
}
