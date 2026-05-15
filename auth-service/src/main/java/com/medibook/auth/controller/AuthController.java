package com.medibook.auth.controller;

import com.medibook.auth.dto.*;
import com.medibook.auth.entity.User;
import com.medibook.auth.service.AuthService;
import com.medibook.auth.service.CloudinaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration, login, and profile management")
public class AuthController {

    private final AuthService authService;
    private final CloudinaryService cloudinaryService;

    // ── Public Endpoints ──

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account (Patient, Provider, or Admin)")
    @ApiResponse(responseCode = "200", description = "Successful registration", content = @Content(schema = @Schema(implementation = LoginResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates a user and returns JWT tokens")
    @ApiResponse(responseCode = "200", description = "Successful login")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset", description = "Sends a password reset link to the user's email")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(Map.of("message", "If an account with that email exists, a reset link has been sent."));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Resets user password using the token from email")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully. You can now login."));
    }

    @GetMapping("/verify-email")
    @Operation(summary = "Verify email", description = "Verifies a user's email address using the verification token")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(Map.of("message", "Email verified successfully!"));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token", description = "Generates a new access token using a valid refresh token")
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RuntimeException("Refresh token is required");
        }
        return ResponseEntity.ok(authService.refreshAccessToken(refreshToken));
    }

    // ── Protected Endpoints ──

    @GetMapping("/profile")
    @Operation(summary = "Get user profile", description = "Retrieves the profile of the currently authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, Object>> getProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(authService.getProfile(user.getId()));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update user profile", description = "Updates the profile of the currently authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(authService.updateProfile(user.getId(), request));
    }

    @PostMapping(value = "/profile-picture", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload profile picture", description = "Uploads a new profile picture for the authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, String>> uploadProfilePicture(
            @AuthenticationPrincipal User user,
            @io.swagger.v3.oas.annotations.Parameter(description = "Image file to upload") @RequestParam("file") MultipartFile file) {
        String imageUrl = cloudinaryService.uploadImage(file, "medibook/profiles");
        String oldUrl = authService.updateProfilePicture(user.getId(), imageUrl);
        if (oldUrl != null && oldUrl.contains("cloudinary")) {
            cloudinaryService.deleteImage(oldUrl);
        }
        return ResponseEntity.ok(Map.of("message", "Profile picture updated", "profilePicture", imageUrl));
    }

    @PutMapping("/change-password")
    @Operation(summary = "Change password", description = "Changes the password for the currently authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(user.getId(), request);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend verification email", description = "Sends a new email verification link")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, String>> resendVerification(@AuthenticationPrincipal User user) {
        authService.resendVerificationEmail(user.getId());
        return ResponseEntity.ok(Map.of("message", "Verification email sent"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Invalidates the user's refresh token")
    public ResponseEntity<Map<String, String>> logout(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
