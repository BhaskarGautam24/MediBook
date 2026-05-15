package com.medibook.payment.controller;

import com.medibook.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Hidden;

import java.util.Map;

/**
 * Internal API — called by appointment-service for payment operations.
 */
@Hidden
@RestController
@RequestMapping("/api/internal/payments")
@RequiredArgsConstructor
public class InternalPaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createPayment(@RequestBody Map<String, Object> data) {
        return ResponseEntity.ok(paymentService.createPayment(
                Long.valueOf(data.get("appointmentId").toString()),
                Long.valueOf(data.get("patientId").toString()),
                Long.valueOf(data.get("providerId").toString()),
                Double.valueOf(data.get("amount").toString()),
                (String) data.get("paymentMode")
        ));
    }

    @PostMapping("/refund/{appointmentId}")
    public ResponseEntity<Map<String, Object>> refundPayment(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(paymentService.refundPayment(appointmentId));
    }

    @PostMapping("/refund-by-provider/{appointmentId}")
    public ResponseEntity<Map<String, Object>> refundPaymentByProvider(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(paymentService.refundPaymentByProvider(appointmentId));
    }

    @PutMapping("/cash-paid/{appointmentId}")
    public ResponseEntity<Map<String, String>> markCashPaymentPaid(@PathVariable Long appointmentId) {
        paymentService.markCashPaymentPaid(appointmentId);
        return ResponseEntity.ok(Map.of("message", "Cash payment marked as paid"));
    }

    @GetMapping("/by-appointment/{appointmentId}")
    public ResponseEntity<Map<String, Object>> getPaymentByAppointment(@PathVariable Long appointmentId) {
        try {
            return ResponseEntity.ok(paymentService.getPaymentByAppointmentId(appointmentId));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(Map.of()); // No payment found
        }
    }
}
