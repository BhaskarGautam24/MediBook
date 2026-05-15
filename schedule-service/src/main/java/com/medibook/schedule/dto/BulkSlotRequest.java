package com.medibook.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request payload for creating multiple slots across a date range")
public class BulkSlotRequest {
    @NotNull @Schema(description = "Start date (YYYY-MM-DD)", example = "2026-06-01")
    private String startDate;
    @NotNull @Schema(description = "End date (YYYY-MM-DD)", example = "2026-06-07")
    private String endDate;
    @NotBlank @Schema(description = "Daily window start time (HH:mm)", example = "09:00")
    private String windowStart;
    @NotBlank @Schema(description = "Daily window end time (HH:mm)", example = "17:00")
    private String windowEnd;
    @NotNull @Min(5) @Schema(description = "Duration per slot in minutes", example = "30")
    private Integer durationMinutes;
    @Schema(description = "Buffer between slots in minutes", example = "5")
    private Integer bufferMinutes;
}
