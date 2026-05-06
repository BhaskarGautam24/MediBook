package com.medibook.controller;

import com.medibook.dto.BookingRequest;
import com.medibook.entity.User;
import com.medibook.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    /**
     * POST /api/appointments — Book an appointment (patient only)
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> bookAppointment(
            @AuthenticationPrincipal User patient,
            @Valid @RequestBody BookingRequest request) {
        return ResponseEntity.ok(appointmentService.bookAppointment(patient, request));
    }

    /**
     * GET /api/appointments/my — Get my appointments (patient only)
     */
    @GetMapping("/my")
    public ResponseEntity<List<Map<String, Object>>> getMyAppointments(
            @AuthenticationPrincipal User patient) {
        return ResponseEntity.ok(appointmentService.getPatientAppointments(patient.getId()));
    }

    /**
     * GET /api/appointments/provider — Get my appointments (provider only)
     */
    @GetMapping("/provider")
    public ResponseEntity<List<Map<String, Object>>> getProviderAppointments(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(appointmentService.getProviderAppointments(user.getId()));
    }

    /**
     * PUT /api/appointments/{id}/cancel — Cancel appointment (patient only)
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelAppointment(
            @PathVariable Long id,
            @AuthenticationPrincipal User patient) {
        Map<String, Object> result = appointmentService.cancelAppointment(id, patient.getId());
        return ResponseEntity.ok(result);
    }

    /**
     * PUT /api/appointments/{id}/accept — Accept appointment (provider only)
     */
    @PutMapping("/{id}/accept")
    public ResponseEntity<Void> acceptAppointment(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        appointmentService.acceptAppointment(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * PUT /api/appointments/{id}/reject — Reject appointment (provider only)
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<Void> rejectAppointment(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        appointmentService.rejectAppointment(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * PUT /api/appointments/{id}/complete — Complete appointment (provider only)
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<Void> completeAppointment(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        appointmentService.completeAppointment(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
