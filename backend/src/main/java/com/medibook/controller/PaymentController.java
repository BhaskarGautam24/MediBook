package com.medibook.controller;

import com.medibook.dto.EarningsResponse;
import com.medibook.dto.PaymentRequest;
import com.medibook.entity.Payment;
import com.medibook.entity.Provider;
import com.medibook.entity.User;
import com.medibook.repository.PaymentRepository;
import com.medibook.service.AppointmentService;
import com.medibook.service.PaymentService;
import com.medibook.service.ProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Payment REST Controller.
 * Handles all payment-related API endpoints.
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final ProviderService providerService;
    private final AppointmentService appointmentService;
    private final PaymentRepository paymentRepository;

    /**
     * POST /api/payments/{paymentId}/confirm — Confirm payment after gateway flow.
     * This is the key endpoint called after patient completes mock/real payment.
     * On success: moves appointment from PENDING_PAYMENT → PENDING.
     * On failure: cancels appointment and releases slot.
     */
    @PostMapping("/{paymentId}/confirm")
    public ResponseEntity<Map<String, Object>> confirmPayment(
            @PathVariable Long paymentId,
            @RequestBody(required = false) PaymentRequest request) {

        // Step 1: Confirm payment with gateway
        Map<String, Object> result = paymentService.confirmPayment(paymentId, request);

        // Step 2: Update appointment based on payment result
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (Boolean.TRUE.equals(result.get("success"))) {
            // Payment succeeded → move appointment to PENDING (doctor approval flow)
            appointmentService.onPaymentConfirmed(payment.getAppointmentId());
        } else {
            // Payment failed → cancel appointment and release slot
            appointmentService.onPaymentFailed(payment.getAppointmentId());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/payments/history — Patient's payment history
     */
    @GetMapping("/history")
    public ResponseEntity<List<Map<String, Object>>> getPaymentHistory(
            @AuthenticationPrincipal User patient) {
        return ResponseEntity.ok(paymentService.getPaymentHistory(patient.getId()));
    }

    /**
     * GET /api/payments/appointment/{appointmentId} — Payment for appointment
     */
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<Map<String, Object>> getPaymentByAppointment(
            @PathVariable Long appointmentId) {
        return ResponseEntity.ok(paymentService.getPaymentByAppointmentId(appointmentId));
    }

    /**
     * GET /api/payments/{paymentId}/invoice — Generate invoice
     */
    @GetMapping("/{paymentId}/invoice")
    public ResponseEntity<Map<String, Object>> generateInvoice(
            @PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.generateInvoice(paymentId));
    }

    /**
     * GET /api/payments/earnings — Provider's earnings dashboard
     */
    @GetMapping("/earnings")
    public ResponseEntity<EarningsResponse> getMyEarnings(
            @AuthenticationPrincipal User user) {
        Provider provider = providerService.getProviderByUserId(user.getId());
        return ResponseEntity.ok(paymentService.getProviderEarnings(provider.getId()));
    }

    /**
     * GET /api/payments/earnings/provider/{providerId} — Admin: provider earnings
     */
    @GetMapping("/earnings/provider/{providerId}")
    public ResponseEntity<EarningsResponse> getProviderEarnings(
            @PathVariable Long providerId) {
        return ResponseEntity.ok(paymentService.getProviderEarnings(providerId));
    }

    /**
     * GET /api/payments/revenue — Admin: total platform revenue
     */
    @GetMapping("/revenue")
    public ResponseEntity<Map<String, Object>> getTotalRevenue() {
        return ResponseEntity.ok(paymentService.getTotalRevenue());
    }
}
