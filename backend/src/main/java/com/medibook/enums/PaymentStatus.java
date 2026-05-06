package com.medibook.enums;

/**
 * Status of a payment transaction.
 * PENDING  → Payment not yet completed
 * PAID     → Payment successful
 * REFUNDED → Payment was refunded (after cancellation)
 * FAILED   → Payment attempt failed
 */
public enum PaymentStatus {
    PENDING,
    PAID,
    REFUNDED,
    FAILED
}
