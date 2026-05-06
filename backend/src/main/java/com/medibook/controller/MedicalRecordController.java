package com.medibook.controller;

import com.medibook.dto.MedicalRecordRequest;
import com.medibook.entity.User;
import com.medibook.service.MedicalRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    private String getRole(User user) {
        return "ROLE_" + user.getRole().name();
    }

    /**
     * POST /api/records - Create a new medical record
     */
    @PostMapping
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Map<String, Object>> createRecord(
            @Valid @RequestBody MedicalRecordRequest request,
            @AuthenticationPrincipal User user) {
        Map<String, Object> record = medicalRecordService.createRecord(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(record);
    }

    /**
     * GET /api/records/appointment/{id}
     */
    @GetMapping("/appointment/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'PROVIDER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getRecordByAppointmentId(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        Map<String, Object> record = medicalRecordService.getRecordByAppointmentId(id, user.getId(), getRole(user));
        return ResponseEntity.ok(record);
    }

    /**
     * GET /api/records/patient/{id}
     */
    @GetMapping("/patient/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'PROVIDER', 'ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getRecordsByPatientId(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        List<Map<String, Object>> records = medicalRecordService.getRecordsByPatientId(id, user.getId(), getRole(user));
        return ResponseEntity.ok(records);
    }

    /**
     * GET /api/records/provider/{id}
     */
    @GetMapping("/provider/{id}")
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getRecordsByProviderId(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        List<Map<String, Object>> records = medicalRecordService.getRecordsByProviderId(id, user.getId(), getRole(user));
        return ResponseEntity.ok(records);
    }

    /**
     * PUT /api/records/{id} - Update a record
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Map<String, Object>> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody MedicalRecordRequest request,
            @AuthenticationPrincipal User user) {
        Map<String, Object> record = medicalRecordService.updateRecord(id, request, user.getId());
        return ResponseEntity.ok(record);
    }

    /**
     * DELETE /api/records/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public ResponseEntity<Void> deleteRecord(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        medicalRecordService.deleteRecord(id, user.getId(), getRole(user));
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/records/followups
     */
    @GetMapping("/followups")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<List<Map<String, Object>>> getFollowUpRecords(
            @AuthenticationPrincipal User user) {
        List<Map<String, Object>> records = medicalRecordService.getFollowUpRecords(user.getId());
        return ResponseEntity.ok(records);
    }
}
