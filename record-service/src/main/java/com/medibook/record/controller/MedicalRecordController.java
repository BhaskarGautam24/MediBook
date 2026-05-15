package com.medibook.record.controller;

import com.medibook.record.dto.MedicalRecordRequest;
import com.medibook.record.service.MedicalRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
@Tag(name = "Medical Records", description = "Endpoints for creating and managing patient medical records")
@SecurityRequirement(name = "bearerAuth")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @PostMapping
    @Operation(summary = "Create record", description = "Creates a new medical record for a completed appointment")
    public ResponseEntity<Map<String, Object>> createRecord(@Valid @RequestBody MedicalRecordRequest request, @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicalRecordService.createRecord(request, userId));
    }

    @GetMapping("/appointment/{id}")
    @Operation(summary = "Get record by appointment", description = "Retrieves a medical record associated with a specific appointment")
    public ResponseEntity<Map<String, Object>> getByAppointment(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId, @RequestHeader(value = "X-User-Role", defaultValue = "") String role) {
        return ResponseEntity.ok(medicalRecordService.getRecordByAppointmentId(id, userId, role));
    }

    @GetMapping("/patient/{id}")
    @Operation(summary = "Get records by patient", description = "Retrieves all medical records for a specific patient")
    public ResponseEntity<List<Map<String, Object>>> getByPatient(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId, @RequestHeader(value = "X-User-Role", defaultValue = "") String role) {
        return ResponseEntity.ok(medicalRecordService.getRecordsByPatientId(id, userId, role));
    }

    @GetMapping("/provider/{id}")
    @Operation(summary = "Get records by provider", description = "Retrieves all medical records created by a specific provider")
    public ResponseEntity<List<Map<String, Object>>> getByProvider(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId, @RequestHeader(value = "X-User-Role", defaultValue = "") String role) {
        return ResponseEntity.ok(medicalRecordService.getRecordsByProviderId(id, userId, role));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update record", description = "Updates an existing medical record")
    public ResponseEntity<Map<String, Object>> updateRecord(@PathVariable Long id, @Valid @RequestBody MedicalRecordRequest request, @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(medicalRecordService.updateRecord(id, request, userId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete record", description = "Deletes a medical record")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId, @RequestHeader(value = "X-User-Role", defaultValue = "") String role) {
        medicalRecordService.deleteRecord(id, userId, role); return ResponseEntity.noContent().build();
    }

    @GetMapping("/followups")
    @Operation(summary = "Get follow-up records", description = "Retrieves records that have upcoming follow-up dates")
    public ResponseEntity<List<Map<String, Object>>> getFollowUps(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(medicalRecordService.getFollowUpRecords(userId));
    }
}
