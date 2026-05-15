package com.medibook.appointment.controller;

import com.medibook.appointment.dto.BookingRequest;
import com.medibook.appointment.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "Endpoints for managing patient appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    @Operation(summary = "Book a new appointment", description = "Allows a patient to book a slot with a provider")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, Object>> bookAppointment(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody BookingRequest request) {
        return ResponseEntity.ok(appointmentService.bookAppointment(userId, request));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my appointments", description = "Retrieves all appointments for the authenticated patient")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<Map<String, Object>>> getMyAppointments(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(appointmentService.getPatientAppointments(userId));
    }

    @GetMapping("/provider")
    @Operation(summary = "Get provider's appointments", description = "Retrieves all appointments for the authenticated provider")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<Map<String, Object>>> getProviderAppointments(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(appointmentService.getProviderAppointments(userId));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel appointment", description = "Cancels an appointment and processes refund if applicable")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, Object>> cancelAppointment(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(appointmentService.cancelAppointment(id, userId));
    }

    @PutMapping("/{id}/accept")
    @Operation(summary = "Accept appointment", description = "Provider accepts a pending appointment")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> acceptAppointment(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        appointmentService.acceptAppointment(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject appointment", description = "Provider rejects a pending appointment and triggers refund")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> rejectAppointment(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        appointmentService.rejectAppointment(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "Complete appointment", description = "Provider marks a scheduled appointment as completed")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> completeAppointment(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        appointmentService.completeAppointment(id, userId);
        return ResponseEntity.noContent().build();
    }
}
