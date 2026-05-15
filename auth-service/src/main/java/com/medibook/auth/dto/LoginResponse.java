package com.medibook.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response returned after successful authentication")
public class LoginResponse {
    @Schema(description = "User's unique ID", example = "1")
    private Long userId;
    @Schema(description = "User's display name", example = "John Doe")
    private String name;
    @Schema(description = "User's email", example = "john@example.com")
    private String email;
    @Schema(description = "User role", example = "PATIENT")
    private String role;
    @Schema(description = "JWT access token")
    private String token;
    @Schema(description = "JWT refresh token")
    private String refreshToken;
    private String profilePicture;
    private Boolean emailVerified;
}
