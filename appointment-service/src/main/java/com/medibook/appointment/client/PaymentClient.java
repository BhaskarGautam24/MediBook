package com.medibook.appointment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@FeignClient(name = "payment-service")
public interface PaymentClient {
    @PostMapping("/api/internal/payments/create")
    Map<String, Object> createPayment(@RequestBody Map<String, Object> paymentData);

    @PostMapping("/api/internal/payments/refund/{appointmentId}")
    Map<String, Object> refundPayment(@PathVariable Long appointmentId);

    @PostMapping("/api/internal/payments/refund-by-provider/{appointmentId}")
    Map<String, Object> refundPaymentByProvider(@PathVariable Long appointmentId);

    @PutMapping("/api/internal/payments/cash-paid/{appointmentId}")
    Map<String, Object> markCashPaymentPaid(@PathVariable Long appointmentId);

    @GetMapping("/api/internal/payments/by-appointment/{appointmentId}")
    Map<String, Object> getPaymentByAppointment(@PathVariable Long appointmentId);
}
