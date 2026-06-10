package com.springboot.MyTodoList.config;

import com.springboot.MyTodoList.model.AppUser;
import com.springboot.MyTodoList.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * On first boot after auth was introduced, existing APP_USER rows have no
 * email or password. This runner assigns a default email and temporary
 * password so every team member can log in immediately.
 *
 * Default password: Talos2026!  — team members should change it via /api/auth/me PATCH.
 */
@Component
public class UserAccountInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(UserAccountInitializer.class);
    private static final String DEFAULT_PASSWORD = "Talos2026!";
    private static final String EMAIL_DOMAIN = "@chatbot.com.mx";

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAccountInitializer(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<AppUser> users = appUserRepository.findAll();
        String hashedDefault = passwordEncoder.encode(DEFAULT_PASSWORD);
        boolean anyUpdated = false;

        for (AppUser user : users) {
            boolean updated = false;

            if (user.getEmail() == null || user.getEmail().isBlank()) {
                String email = toEmail(user.getName());
                user.setEmail(email);
                updated = true;
            }

            if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
                user.setPasswordHash(hashedDefault);
                updated = true;
            }

            if (updated) {
                appUserRepository.save(user);
                logger.info("Initialized account — name: '{}' | email: '{}' | temp password: {}",
                        user.getName(), user.getEmail(), DEFAULT_PASSWORD);
                anyUpdated = true;
            }
        }

        if (!anyUpdated) {
            logger.info("UserAccountInitializer: all accounts already initialized, nothing to do.");
        }
    }

    private String toEmail(String name) {
        if (name == null || name.isBlank()) return "user" + System.currentTimeMillis() + EMAIL_DOMAIN;
        return name.trim().toLowerCase()
                .replaceAll("\\s+", ".")
                .replaceAll("[^a-z0-9.]", "") + EMAIL_DOMAIN;
    }
}
