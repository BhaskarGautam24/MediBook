package com.medibook.appointment.controller;

import com.medibook.appointment.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Hidden;

import java.util.List;
import java.util.Map;

/**
 * Internal API for service-to-service calls.
 */
@Hidden
@RestController
@RequestMapping("/api/internal/appointments")
@RequiredArgsConstructor
public class InternalAppointmentController {

    private final AppointmentService appointmentService;

    /** Called by review-service / record-service to get appointment details */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAppointmentById(@PathVariable Long id) {
        // Reuse the existing toAppointmentMap logic via getAllAppointments or direct repo
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

    /** Called by schedule-service when a slot expires */
    @PostMapping("/slot-expired/{slotId}")
    public ResponseEntity<Map<String, String>> handleSlotExpired(@PathVariable Long slotId) {
        appointmentService.handleSlotExpired(slotId);
        return ResponseEntity.ok(Map.of("message", "Slot expired handled"));
    }

    /** Called by payment-service when payment is confirmed */
    @PostMapping("/payment-confirmed/{appointmentId}")
    public ResponseEntity<Map<String, String>> onPaymentConfirmed(@PathVariable Long appointmentId) {
        appointmentService.onPaymentConfirmed(appointmentId);
        return ResponseEntity.ok(Map.of("message", "Payment confirmed processed"));
    }

    /** Called by payment-service when payment fails */
    @PostMapping("/payment-failed/{appointmentId}")
    public ResponseEntity<Map<String, String>> onPaymentFailed(@PathVariable Long appointmentId) {
        appointmentService.onPaymentFailed(appointmentId);
        return ResponseEntity.ok(Map.of("message", "Payment failure processed"));
    }
}
