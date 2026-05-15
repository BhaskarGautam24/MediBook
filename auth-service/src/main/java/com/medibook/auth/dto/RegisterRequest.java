package com.medibook.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Request payload for user registration")
public class RegisterRequest {
    @NotBlank(message = "Name is required")
    @Schema(description = "Full name of the user", example = "John Doe")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "User's email address", example = "john@example.com")
    private String email;

    @Schema(description = "Phone number (optional)", example = "+91-9876543210")
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Schema(description = "User password (min 6 characters)", example = "SecurePass123!")
    private String password;

    @NotBlank(message = "Role is required")
    @Schema(description = "User role (PATIENT, PROVIDER, ADMIN)", example = "PATIENT")
    private String role;

    // Provider-specific fields (optional, used when role=PROVIDER)
    @Schema(description = "Provider specialization (if role=PROVIDER)", example = "Cardiologist")
    private String specialization;
    @Schema(description = "Years of experience (if role=PROVIDER)", example = "10")
    private Integer experienceYears;
    @Schema(description = "Clinic name (if role=PROVIDER)", example = "Heart Care Center")
    private String clinicName;
    @Schema(description = "Clinic address (if role=PROVIDER)", example = "123 Main St, City")
    private String clinicAddress;
}
