package com.medibook.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO returned after successful login/register.
 * Contains JWT token and basic user info.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private Long userId;
    private String fullName;
    private String email;
    private String role;
    private String profilePicUrl;
}
