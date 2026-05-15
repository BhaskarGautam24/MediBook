package com.medibook.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@FeignClient(name = "appointment-service")
public interface AppointmentClient {
    @PostMapping("/api/internal/appointments/payment-confirmed/{appointmentId}")
    Map<String, String> onPaymentConfirmed(@PathVariable Long appointmentId);

    @PostMapping("/api/internal/appointments/payment-failed/{appointmentId}")
    Map<String, String> onPaymentFailed(@PathVariable Long appointmentId);
}
