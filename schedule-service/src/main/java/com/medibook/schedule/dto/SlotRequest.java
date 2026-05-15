package com.medibook.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request payload for creating a single slot")
public class SlotRequest {
    @NotNull
    @Schema(description = "Date of the slot (YYYY-MM-DD)", example = "2026-06-15")
    private String date;
    @NotBlank
    @Schema(description = "Start time (HH:mm)", example = "09:00")
    private String startTime;
    @NotBlank
    @Schema(description = "End time (HH:mm)", example = "09:30")
    private String endTime;
    @Schema(description = "Duration in minutes (optional)", example = "30")
    private Integer durationMinutes;
}
