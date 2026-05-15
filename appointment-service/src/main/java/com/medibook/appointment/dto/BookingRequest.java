package com.medibook.appointment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request payload for booking an appointment")
public class BookingRequest {
    @NotNull
    @Schema(description = "ID of the slot to book", example = "1")
    private Long slotId;
    @NotNull
    @Schema(description = "ID of the healthcare provider", example = "5")
    private Long providerId;
    @Schema(description = "Payment mode: CASH or ONLINE", example = "CASH", defaultValue = "CASH")
    private String paymentMode = "CASH";
}
