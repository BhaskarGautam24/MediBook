package com.medibook.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminProviderDto {
    private Long id;
    private String name;
    private String email;
    private String specialization;
    private Boolean isVerified;
}
