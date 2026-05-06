package com.medibook.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminStatsDto {
    private long totalUsers;
    private long totalProviders;
    private long totalAppointments;
    private long completedAppointments;
}
