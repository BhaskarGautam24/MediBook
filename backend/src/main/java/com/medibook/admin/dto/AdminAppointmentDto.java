package com.medibook.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminAppointmentDto {
    private Long id;
    private String patientName;
    private String providerName;
    private String appointmentDate;
    private String status;
}
