package com.medibook.appointment.controller;

import com.medibook.appointment.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/appointments")
@RequiredArgsConstructor
@Tag(name = "Admin - Appointments", description = "Admin endpoints for appointment oversight")
@SecurityRequirement(name = "bearerAuth")
public class AdminAppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping
    @Operation(summary = "Get all appointments", description = "Retrieves all appointments across the platform")
    public ResponseEntity<List<Map<String, Object>>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }
}
