package com.medibook.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request payload for password reset initiation")
public class ForgotPasswordRequest {
    @NotBlank @Email
    @Schema(description = "Email address of the account", example = "john@example.com")
    private String email;
}
