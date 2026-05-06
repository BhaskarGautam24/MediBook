package com.medibook.service;

import com.medibook.config.JwtUtil;
import com.medibook.dto.LoginRequest;
import com.medibook.dto.LoginResponse;
import com.medibook.dto.RegisterRequest;
import com.medibook.entity.Provider;
import com.medibook.entity.User;
import com.medibook.enums.ProviderStatus;
import com.medibook.enums.Role;
import com.medibook.repository.ProviderRepository;
import com.medibook.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ProviderRepository providerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        Role role = Role.valueOf(request.getRole().toUpperCase());

        // Create user
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();
        user = userRepository.save(user);

        // If provider, create provider record with PENDING status
        if (role == Role.PROVIDER) {
            Provider provider = Provider.builder()
                    .user(user)
                    .specialization(request.getSpecialization())
                    .experienceYears(request.getExperienceYears())
                    .clinicName(request.getClinicName())
                    .clinicAddress(request.getClinicAddress())
                    .isVerified(false)
                    .status(ProviderStatus.PENDING)
                    .build();
            providerRepository.save(provider);
        }

        // Generate JWT
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        return LoginResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .token(token)
                .build();
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new RuntimeException("Your account has been suspended. Please contact the administrator.");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        return LoginResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .token(token)
                .build();
    }
}
