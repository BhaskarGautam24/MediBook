package com.medibook.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request payload for user login")
public class LoginRequest {
    @NotBlank @Email
    @Schema(description = "Registered email address", example = "john@example.com")
    private String email;
    @NotBlank
    @Schema(description = "Account password", example = "password123")
    private String password;
}
