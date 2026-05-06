package com.medibook.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminUserDto {
    private Long id;
    private String name;
    private String email;
    private String role;
    private Boolean isActive;
}
