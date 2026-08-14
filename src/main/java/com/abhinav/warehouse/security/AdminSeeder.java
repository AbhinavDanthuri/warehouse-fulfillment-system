package com.abhinav.warehouse.security;

import com.abhinav.warehouse.entity.Role;
import com.abhinav.warehouse.entity.User;
import com.abhinav.warehouse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Creates a first admin so there is someone who can create everyone else. */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        userRepository.save(User.builder()
                .email("admin@warehouse.local")
                .passwordHash(passwordEncoder.encode("admin12345"))
                .fullName("Seed Admin")
                .role(Role.ADMIN)
                .build());

        log.warn("Seeded admin@warehouse.local / admin12345 — dev only, change before deploying");
    }
}
