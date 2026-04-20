package com.medibook.auth.service;

import com.medibook.auth.config.JwtUtil;
import com.medibook.auth.dto.*;
import com.medibook.auth.entity.User;
import com.medibook.auth.exception.InvalidCredentialsException;
import com.medibook.auth.exception.UserAlreadyExistsException;
import com.medibook.auth.exception.UserNotFoundException;
import com.medibook.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of AuthService — handles registration, login, JWT lifecycle,
 * profile management, and account operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered: " + request.getEmail());
        }

        // Build user entity
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(User.Role.valueOf(request.getRole().toUpperCase()))
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {} with role {}", savedUser.getEmail(), savedUser.getRole());

        // Generate JWT and return response
        String token = jwtUtil.generateToken(savedUser.getUserId(), savedUser.getEmail(), savedUser.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .userId(savedUser.getUserId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .profilePicUrl(savedUser.getProfilePicUrl())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Check if account is active
        if (!user.getIsActive()) {
            throw new InvalidCredentialsException("Account is deactivated. Please contact support.");
        }

        log.info("User logged in: {}", user.getEmail());

        // Generate JWT and return response
        String token = jwtUtil.generateToken(user.getUserId(), user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .profilePicUrl(user.getProfilePicUrl())
                .build();
    }

    @Override
    public UserProfileDto validateToken(String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new InvalidCredentialsException("Invalid or expired token");
        }

        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found for token"));

        return mapToProfileDto(user);
    }

    @Override
    public UserProfileDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        return mapToProfileDto(user);
    }

    @Override
    public UserProfileDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        return mapToProfileDto(user);
    }

    @Override
    @Transactional
    public UserProfileDto updateProfile(Long userId, UserProfileDto profileDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        // Update only non-null fields
        if (profileDto.getFullName() != null) user.setFullName(profileDto.getFullName());
        if (profileDto.getPhone() != null) user.setPhone(profileDto.getPhone());
        if (profileDto.getProfilePicUrl() != null) user.setProfilePicUrl(profileDto.getProfilePicUrl());

        User updated = userRepository.save(user);
        log.info("Profile updated for user: {}", updated.getEmail());
        return mapToProfileDto(updated);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for user: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void deactivateAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        user.setIsActive(false);
        userRepository.save(user);
        log.info("Account deactivated for user: {}", user.getEmail());
    }

    @Override
    public List<UserProfileDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToProfileDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserProfileDto> getUsersByRole(String role) {
        User.Role userRole = User.Role.valueOf(role.toUpperCase());
        return userRepository.findAllByRole(userRole).stream()
                .map(this::mapToProfileDto)
                .collect(Collectors.toList());
    }

    /**
     * Maps User entity to UserProfileDto.
     */
    private UserProfileDto mapToProfileDto(User user) {
        return UserProfileDto.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .profilePicUrl(user.getProfilePicUrl())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
