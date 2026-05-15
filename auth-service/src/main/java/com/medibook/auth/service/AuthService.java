package com.medibook.auth.service;

import com.medibook.auth.client.ProviderClient;
import com.medibook.auth.config.JwtUtil;
import com.medibook.auth.dto.*;
import com.medibook.auth.entity.*;
import com.medibook.auth.enums.Role;
import com.medibook.auth.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;
    private final ProviderClient providerClient;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    // --- Register ---

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        Role role = Role.valueOf(request.getRole().toUpperCase());

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .authProvider("LOCAL")
                .emailVerified(false)
                .build();
        user = userRepository.save(user);

        // If provider, create provider profile via provider-service
        if (role == Role.PROVIDER) {
            try {
                Map<String, Object> providerData = new HashMap<>();
                providerData.put("userId", user.getId());
                providerData.put("specialization", request.getSpecialization());
                providerData.put("experienceYears", request.getExperienceYears());
                providerData.put("clinicName", request.getClinicName());
                providerData.put("clinicAddress", request.getClinicAddress());
                providerClient.createProvider(providerData);
            } catch (Exception e) {
                log.warn("Failed to create provider profile: {}. Will be retried.", e.getMessage());
            }
        }

        sendVerificationEmail(user);

        String accessToken = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return LoginResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .token(accessToken)
                .refreshToken(refreshToken.getToken())
                .profilePicture(user.getProfilePicture())
                .emailVerified(user.getEmailVerified())
                .build();
    }

    // --- Login ---

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (user.getAuthProvider() != null && !"LOCAL".equals(user.getAuthProvider())) {
            throw new RuntimeException("This account uses Google login. Please use 'Continue with Google'.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new RuntimeException("Your account has been suspended. Please contact the administrator.");
        }

        String accessToken = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return LoginResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .token(accessToken)
                .refreshToken(refreshToken.getToken())
                .profilePicture(user.getProfilePicture())
                .emailVerified(user.getEmailVerified())
                .build();
    }

    // --- Google OAuth2 Login ---

    @Transactional
    public LoginResponse processOAuthLogin(String email, String name, String picture) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            user = User.builder()
                    .name(name)
                    .email(email)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .role(Role.PATIENT)
                    .authProvider("GOOGLE")
                    .emailVerified(true)
                    .profilePicture(picture)
                    .build();
            user = userRepository.save(user);
            log.info("New Google user created: {}", email);
        } else {
            if (user.getProfilePicture() == null && picture != null) {
                user.setProfilePicture(picture);
                userRepository.save(user);
            }
        }

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new RuntimeException("Your account has been suspended.");
        }

        String accessToken = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return LoginResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .token(accessToken)
                .refreshToken(refreshToken.getToken())
                .profilePicture(user.getProfilePicture())
                .emailVerified(user.getEmailVerified())
                .build();
    }

    // --- Profile Update ---

    @Transactional
    public Map<String, Object> updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        userRepository.save(user);

        return Map.of(
                "message", "Profile updated successfully",
                "name", user.getName(),
                "email", user.getEmail(),
                "phone", user.getPhone() != null ? user.getPhone() : "",
                "profilePicture", user.getProfilePicture() != null ? user.getProfilePicture() : ""
        );
    }

    @Transactional
    public String updateProfilePicture(Long userId, String imageUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String oldPicture = user.getProfilePicture();
        user.setProfilePicture(imageUrl);
        userRepository.save(user);
        return oldPicture;
    }

    // --- Change Password ---

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getAuthProvider() != null && !"LOCAL".equals(user.getAuthProvider())) {
            throw new RuntimeException("Google accounts cannot change password here.");
        }
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New passwords do not match");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        refreshTokenService.deleteAllByUser(user);
        log.info("Password changed for user: {}", user.getEmail());
    }

    // --- Forgot Password ---

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null || (user.getAuthProvider() != null && !"LOCAL".equals(user.getAuthProvider()))) {
            return;
        }

        passwordResetTokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(30))
                .build();
        passwordResetTokenRepository.save(resetToken);

        String resetLink = frontendUrl + "/reset-password?token=" + token;
        String emailBody = String.format(
                "Hi %s,\n\nYou requested a password reset for your MediBook account.\n\n" +
                "Click the link below to reset your password:\n%s\n\n" +
                "This link expires in 30 minutes.\n\nIf you didn't request this, please ignore this email.\n\n— MediBook Team",
                user.getName(), resetLink
        );

        emailService.sendEmail(user.getEmail(), "MediBook - Reset Your Password", emailBody);
        log.info("Password reset email sent to: {}", user.getEmail());
    }

    // --- Reset Password ---

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset link"));

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new RuntimeException("Reset link has expired. Please request a new one.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        passwordResetTokenRepository.delete(resetToken);
        refreshTokenService.deleteAllByUser(user);
        log.info("Password reset completed for user: {}", user.getEmail());
    }

    // --- Email Verification ---

    @Transactional
    public void sendVerificationEmail(User user) {
        if (Boolean.TRUE.equals(user.getEmailVerified()) ||
            (user.getAuthProvider() != null && !"LOCAL".equals(user.getAuthProvider()))) {
            return;
        }

        emailVerificationTokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();
        emailVerificationTokenRepository.save(verificationToken);

        String verifyLink = frontendUrl + "/verify-email?token=" + token;
        String emailBody = String.format(
                "Hi %s,\n\nWelcome to MediBook! Please verify your email address.\n\n" +
                "Click the link below to activate your account:\n%s\n\n" +
                "This link expires in 24 hours.\n\n— MediBook Team",
                user.getName(), verifyLink
        );

        emailService.sendEmail(user.getEmail(), "MediBook - Verify Your Email", emailBody);
        log.info("Verification email sent to: {}", user.getEmail());
    }

    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification link"));

        if (verificationToken.isExpired()) {
            emailVerificationTokenRepository.delete(verificationToken);
            throw new RuntimeException("Verification link has expired. Please request a new one.");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        emailVerificationTokenRepository.delete(verificationToken);
        log.info("Email verified for user: {}", user.getEmail());
    }

    @Transactional
    public void resendVerificationEmail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new RuntimeException("Email is already verified");
        }
        sendVerificationEmail(user);
    }

    // --- Refresh Token ---

    public LoginResponse refreshAccessToken(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(refreshTokenStr);
        User user = refreshToken.getUser();
        String newAccessToken = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        return LoginResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .token(newAccessToken)
                .refreshToken(refreshToken.getToken())
                .profilePicture(user.getProfilePicture())
                .emailVerified(user.getEmailVerified())
                .build();
    }

    // --- Logout ---

    public void logout(String refreshTokenStr) {
        refreshTokenService.deleteByToken(refreshTokenStr);
    }

    // --- Get Profile ---

    public Map<String, Object> getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "phone", user.getPhone() != null ? user.getPhone() : "",
                "role", user.getRole().name(),
                "profilePicture", user.getProfilePicture() != null ? user.getProfilePicture() : "",
                "emailVerified", Boolean.TRUE.equals(user.getEmailVerified()),
                "authProvider", user.getAuthProvider() != null ? user.getAuthProvider() : "LOCAL"
        );
    }
}
