package com.medibook.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request for generating recurring slots (DAILY or WEEKLY).
 *
 * Example: recurrenceType=WEEKLY, startDate=2026-05-05, endDate=2026-06-30,
 * startTime=10:00, endTime=10:30 → creates one 30-min slot every Monday for 8 weeks
 */
@Data
public class RecurringSlotRequest {

    @NotNull(message = "Recurrence type is required")
    private String recurrenceType; // DAILY or WEEKLY

    @NotNull(message = "Start date is required")
    private String startDate; // YYYY-MM-DD

    @NotNull(message = "End date is required")
    private String endDate; // YYYY-MM-DD

    @NotBlank(message = "Start time is required")
    private String startTime; // HH:mm

    @NotBlank(message = "End time is required")
    private String endTime; // HH:mm
}
