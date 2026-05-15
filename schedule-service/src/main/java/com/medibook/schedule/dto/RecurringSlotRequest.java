package com.medibook.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request payload for creating recurring weekly slots")
public class RecurringSlotRequest {
    @NotNull @Schema(description = "Recurrence type: DAILY, WEEKLY, etc.", example = "WEEKLY")
    private String recurrenceType;
    @NotNull @Schema(description = "Start date (YYYY-MM-DD)", example = "2026-06-01")
    private String startDate;
    @NotNull @Schema(description = "End date (YYYY-MM-DD)", example = "2026-06-30")
    private String endDate;
    @NotBlank @Schema(description = "Start time (HH:mm)", example = "10:00")
    private String startTime;
    @NotBlank @Schema(description = "End time (HH:mm)", example = "10:30")
    private String endTime;
}
