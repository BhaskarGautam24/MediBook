package com.medibook.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request payload for updating user profile")
public class ProfileUpdateRequest {
    @Schema(description = "Updated display name", example = "Jane Doe")
    private String name;
    @Schema(description = "Updated phone number", example = "+91-9876543210")
    private String phone;
}
