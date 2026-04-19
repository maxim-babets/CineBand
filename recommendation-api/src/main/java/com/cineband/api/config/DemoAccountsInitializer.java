package com.cineband.api.config;

import com.cineband.api.repo.UserAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DemoAccountsInitializer {

    @Bean
    CommandLineRunner seedDemoPasswords(UserAccountRepository users, PasswordEncoder encoder) {
        return args -> {
            for (int id = 1; id <= 5; id++) {
                final int uid = id;
                users.findById(uid).ifPresent(u -> {
                    boolean changed = false;
                    if (u.getPasswordHash() == null || u.getPasswordHash().isBlank()) {
                        u.setPasswordHash(encoder.encode("demo123"));
                        changed = true;
                    }
                    if (u.getNick() == null || u.getNick().isBlank()) {
                        u.setNick("demo" + uid);
                        changed = true;
                    }
                    if (changed) {
                        users.save(u);
                    }
                });
            }
        };
    }
}
