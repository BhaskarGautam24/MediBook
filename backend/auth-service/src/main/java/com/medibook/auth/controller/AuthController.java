package com.medibook.auth.controller;

import com.medibook.auth.dto.*;
import com.medibook.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for authentication and user management.
 * Exposes all auth endpoints under /api/v1/auth.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration, login, JWT, and profile management")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a patient or provider account and returns JWT token")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates user and returns JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate JWT token", description = "Validates the Bearer token and returns user profile")
    public ResponseEntity<UserProfileDto> validateToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        UserProfileDto profile = authService.validateToken(token);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/profile/{userId}")
    @Operation(summary = "Get user profile", description = "Returns user profile by ID")
    public ResponseEntity<UserProfileDto> getProfile(@PathVariable Long userId) {
        UserProfileDto profile = authService.getUserById(userId);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile/{userId}")
    @Operation(summary = "Update user profile", description = "Updates name, phone, and profile picture")
    public ResponseEntity<UserProfileDto> updateProfile(
            @PathVariable Long userId,
            @RequestBody UserProfileDto profileDto) {
        UserProfileDto updated = authService.updateProfile(userId, profileDto);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/password/{userId}")
    @Operation(summary = "Change password", description = "Changes user password after verifying current password")
    public ResponseEntity<Map<String, String>> changePassword(
            @PathVariable Long userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(userId, request);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @PutMapping("/deactivate/{userId}")
    @Operation(summary = "Deactivate account", description = "Deactivates the user account")
    public ResponseEntity<Map<String, String>> deactivateAccount(@PathVariable Long userId) {
        authService.deactivateAccount(userId);
        return ResponseEntity.ok(Map.of("message", "Account deactivated successfully"));
    }

    @GetMapping("/users")
    @Operation(summary = "Get all users (Admin)", description = "Returns all registered users — admin only")
    public ResponseEntity<List<UserProfileDto>> getAllUsers() {
        List<UserProfileDto> users = authService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/role/{role}")
    @Operation(summary = "Get users by role (Admin)", description = "Returns users filtered by role — admin only")
    public ResponseEntity<List<UserProfileDto>> getUsersByRole(@PathVariable String role) {
        List<UserProfileDto> users = authService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }
}
