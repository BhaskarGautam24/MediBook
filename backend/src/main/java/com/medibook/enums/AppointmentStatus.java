package com.medibook.enums;

public enum AppointmentStatus {
    PENDING_PAYMENT,  // Waiting for online payment
    PENDING,
    SCHEDULED,
    BOOKED,
    COMPLETED,
    CANCELLED,
    REJECTED,
    NO_SHOW
}
