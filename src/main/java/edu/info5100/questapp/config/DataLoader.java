package edu.info5100.questapp.config;

import edu.info5100.questapp.user.Role;
import edu.info5100.questapp.user.User;
import edu.info5100.questapp.user.UserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Loads initial test users if the database is empty.
 * Useful for development. Remove or disable in production.
 */
@Configuration
public class DataLoader {

    @Bean
    public ApplicationRunner loadData(UserRepository userRepository, PasswordEncoder encoder) {
        return args -> {
            if (userRepository.count() > 0) return;

            User giver = new User("giver1", "giver@test.com", encoder.encode("password"), Role.GIVER);
            giver.setBalance(1000.0); // Initial balance for giver
            userRepository.save(giver);

            User taker = new User("taker1", "taker@test.com", encoder.encode("password"), Role.TAKER);
            taker.setBalance(100.0); // Initial balance for taker
            userRepository.save(taker);

            User admin = new User("admin1", "admin@test.com", encoder.encode("password"), Role.ADMIN);
            admin.setBalance(0.0);
            userRepository.save(admin);
        };
    }
}
