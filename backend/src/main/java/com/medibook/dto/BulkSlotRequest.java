package com.medibook.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request for generating multiple slots from a date range + time window + duration.
 *
 * Example: startDate=2026-05-05, endDate=2026-05-09, windowStart=09:00, windowEnd=17:00, durationMinutes=30
 * → generates 16 slots per day × 5 days = 80 slots
 */
@Data
public class BulkSlotRequest {

    @NotNull(message = "Start date is required")
    private String startDate; // YYYY-MM-DD

    @NotNull(message = "End date is required")
    private String endDate; // YYYY-MM-DD

    @NotBlank(message = "Window start time is required")
    private String windowStart; // HH:mm

    @NotBlank(message = "Window end time is required")
    private String windowEnd; // HH:mm

    @NotNull(message = "Slot duration is required")
    @Min(value = 5, message = "Minimum slot duration is 5 minutes")
    private Integer durationMinutes;

    private Integer bufferMinutes; // Optional gap between slots (default 0)
}
