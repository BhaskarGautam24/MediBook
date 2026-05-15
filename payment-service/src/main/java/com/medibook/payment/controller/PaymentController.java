package com.medibook.payment.controller;

import com.medibook.payment.client.AppointmentClient;
import com.medibook.payment.client.ProviderClient;
import com.medibook.payment.dto.EarningsResponse;
import com.medibook.payment.dto.PaymentRequest;
import com.medibook.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Endpoints for payment processing, history, and earnings")
public class PaymentController {

    private final PaymentService paymentService;
    private final AppointmentClient appointmentClient;
    private final ProviderClient providerClient;

    /**
     * Confirm payment after gateway flow.
     * On success → notifies appointment-service to move PENDING_PAYMENT → PENDING.
     * On failure → notifies appointment-service to cancel + release slot.
     */
    @PostMapping("/{paymentId}/confirm")
    @Operation(summary = "Confirm payment", description = "Confirms a payment after gateway checkout and notifies appointment service")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, Object>> confirmPayment(
            @PathVariable Long paymentId,
            @RequestBody(required = false) PaymentRequest request) {

        Map<String, Object> result = paymentService.confirmPayment(paymentId, request);

        // Callback to appointment-service
        Long appointmentId = result.get("appointmentId") != null
                ? Long.valueOf(result.get("appointmentId").toString()) : null;

        if (appointmentId != null) {
            try {
                if (Boolean.TRUE.equals(result.get("success"))) {
                    appointmentClient.onPaymentConfirmed(appointmentId);
                } else {
                    appointmentClient.onPaymentFailed(appointmentId);
                }
            } catch (Exception e) {
                log.warn("Failed to notify appointment-service: {}", e.getMessage());
            }
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    @Operation(summary = "Get payment history", description = "Retrieves the payment history for the authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<Map<String, Object>>> getPaymentHistory(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(paymentService.getPaymentHistory(userId));
    }

    @GetMapping("/appointment/{appointmentId}")
    @Operation(summary = "Get payment by appointment", description = "Retrieves payment details for a specific appointment")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, Object>> getPaymentByAppointment(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(paymentService.getPaymentByAppointmentId(appointmentId));
    }

    @GetMapping("/{paymentId}/invoice")
    @Operation(summary = "Generate invoice", description = "Generates an invoice for a completed payment")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, Object>> generateInvoice(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.generateInvoice(paymentId));
    }

    @GetMapping("/earnings")
    @Operation(summary = "Get my earnings", description = "Retrieves earnings summary for the authenticated provider")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<EarningsResponse> getMyEarnings(@RequestHeader("X-User-Id") Long userId) {
        Map<String, Object> provider = providerClient.getProviderByUserId(userId);
        Long providerId = Long.valueOf(provider.get("id").toString());
        return ResponseEntity.ok(paymentService.getProviderEarnings(providerId));
    }

    @GetMapping("/earnings/provider/{providerId}")
    @Operation(summary = "Get provider earnings", description = "Retrieves earnings summary for a specific provider")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<EarningsResponse> getProviderEarnings(@PathVariable Long providerId) {
        return ResponseEntity.ok(paymentService.getProviderEarnings(providerId));
    }

    @GetMapping("/revenue")
    @Operation(summary = "Get total revenue", description = "Retrieves total platform revenue statistics")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, Object>> getTotalRevenue() {
        return ResponseEntity.ok(paymentService.getTotalRevenue());
    }
}
