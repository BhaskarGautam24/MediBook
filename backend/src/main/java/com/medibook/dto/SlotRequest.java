package com.medibook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SlotRequest {

    @NotNull(message = "Date is required")
    private String date; // YYYY-MM-DD

    @NotBlank(message = "Start time is required")
    private String startTime; // HH:mm

    @NotBlank(message = "End time is required")
    private String endTime; // HH:mm

    private Integer durationMinutes; // Optional, auto-calculated if absent
}
