package com.medibook.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request payload for password reset confirmation")
public class ResetPasswordRequest {
    @NotBlank
    @Schema(description = "Password reset token from email")
    private String token;
    @NotBlank @Size(min = 6)
    @Schema(description = "New password (min 6 chars)", example = "newpass123")
    private String newPassword;
    @NotBlank
    @Schema(description = "Must match newPassword", example = "newpass123")
    private String confirmPassword;
}
