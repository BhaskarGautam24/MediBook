package com.medibook.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for user profile data — used for profile viewing and updates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {

    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private String profilePicUrl;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
