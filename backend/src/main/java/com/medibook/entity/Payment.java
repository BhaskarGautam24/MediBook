package com.medibook.entity;

import com.medibook.enums.PaymentMode;
import com.medibook.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Payment entity — stores every payment transaction.
 * One appointment maps to exactly one payment (1:1).
 */
@Entity
@Table(name = "payments")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which appointment this payment is for (1:1)
    @Column(name = "appointment_id", nullable = false, unique = true)
    private Long appointmentId;

    // Who is paying
    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    // Who receives the money
    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    // Payment amount in the given currency
    @Column(nullable = false)
    private Double amount;

    // Current status of the payment
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    // How the patient is paying
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMode mode;

    // Transaction ID from payment gateway (Razorpay/mock)
    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    // Order ID from payment gateway
    @Column(name = "gateway_order_id", length = 100)
    private String gatewayOrderId;

    // Currency code (default INR)
    @Column(length = 10)
    @Builder.Default
    private String currency = "INR";

    // When payment was completed
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    // When refund was processed
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    // Optional notes (reason for refund, etc.)
    @Column(length = 500)
    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
