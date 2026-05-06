package com.medibook.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminRecordDto {
    private Long id;
    private String patientName;
    private String providerName;
    private String diagnosis;
    private String prescription;
    private String createdAt;
}
