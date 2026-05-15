package com.medibook.record.controller;

import com.medibook.record.service.MedicalRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin endpoint for medical records audit.
 * Gateway routes /api/admin/records/** here.
 */
@RestController
@RequestMapping("/api/admin/records")
@RequiredArgsConstructor
@Tag(name = "Admin - Medical Records", description = "Admin endpoints for medical records audit")
@SecurityRequirement(name = "bearerAuth")
public class AdminRecordController {

    private final MedicalRecordService medicalRecordService;

    @GetMapping
    @Operation(summary = "Get all records", description = "Retrieves all medical records across the platform for admin audit")
    public ResponseEntity<List<Map<String, Object>>> getAllRecords(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String role) {
        return ResponseEntity.ok(medicalRecordService.getAllRecords(userId, role));
    }
}
