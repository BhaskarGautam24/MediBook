package com.medibook.auth.service;

import com.medibook.auth.dto.*;

import java.util.List;

/**
 * AuthService interface — declares all authentication and user management operations.
 * Mirrors the case study Section 4.1 AuthService contract.
 */
public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserProfileDto validateToken(String token);

    UserProfileDto getUserById(Long userId);

    UserProfileDto getUserByEmail(String email);

    UserProfileDto updateProfile(Long userId, UserProfileDto profileDto);

    void changePassword(Long userId, ChangePasswordRequest request);

    void deactivateAccount(Long userId);

    List<UserProfileDto> getAllUsers();

    List<UserProfileDto> getUsersByRole(String role);
}
