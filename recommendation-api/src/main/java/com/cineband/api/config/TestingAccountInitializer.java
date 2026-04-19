package com.cineband.api.config;

import com.cineband.api.domain.UserAccount;
import com.cineband.api.repo.UserAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Ensures a documented QA account exists (see {@code docs/TESTING_ACCOUNT.md}).
 * Disable in production: {@code cineband.testing-user.enabled=false}.
 */
@Configuration
public class TestingAccountInitializer {

    @Bean
    @ConditionalOnProperty(name = "cineband.testing-user.enabled", havingValue = "true", matchIfMissing = true)
    CommandLineRunner seedTestingAccount(
            UserAccountRepository users,
            PasswordEncoder encoder,
            org.springframework.core.env.Environment env
    ) {
        return args -> {
            String email = env.getProperty("cineband.testing-user.email", "cineband.tester@example.com");
            String password = env.getProperty("cineband.testing-user.password", "CineBandTest!2026");
            String nick = env.getProperty("cineband.testing-user.nick", "cineband_tester");
            String display = env.getProperty("cineband.testing-user.display-name", "CineBand Tester");

            if (users.findByEmailIgnoreCase(email).isPresent()) {
                return;
            }
            if (users.existsByNickIgnoreCase(nick)) {
                return;
            }

            UserAccount u = new UserAccount();
            u.setEmail(email.trim().toLowerCase());
            u.setNick(nick);
            u.setDisplayName(display);
            u.setPasswordHash(encoder.encode(password));
            users.save(u);
        };
    }
}
