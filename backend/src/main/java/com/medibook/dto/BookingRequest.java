package com.medibook.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingRequest {

    @NotNull(message = "Slot ID is required")
    private Long slotId;

    @NotNull(message = "Provider ID is required")
    private Long providerId;

    // Payment mode: UPI, CARD, WALLET, NETBANKING, CASH
    // If not provided, defaults to CASH (pay at clinic)
    private String paymentMode = "CASH";
}
