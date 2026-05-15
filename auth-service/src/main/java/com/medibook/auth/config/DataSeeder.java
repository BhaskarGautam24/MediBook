package com.medibook.auth.config;

import com.medibook.auth.entity.User;
import com.medibook.auth.enums.Role;
import com.medibook.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        User admin = userRepository.findByEmail("admin@medibook.com").orElse(null);
        if (admin == null) {
            admin = User.builder()
                    .name("Main Admin")
                    .email("admin@medibook.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .isActive(true)
                    .build();
            userRepository.save(admin);
            log.info("Default main admin created — email: admin@medibook.com, password: admin123");
        } else if (Boolean.FALSE.equals(admin.getIsActive())) {
            admin.setIsActive(true);
            userRepository.save(admin);
            log.info("Main admin un-suspended.");
        }
    }
}
