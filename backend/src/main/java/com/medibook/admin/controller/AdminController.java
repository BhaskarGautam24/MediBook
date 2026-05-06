package com.medibook.admin.controller;

import com.medibook.admin.dto.*;
import com.medibook.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<AdminStatsDto> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserDto>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/users/{id}/suspend")
    public ResponseEntity<Void> suspendUser(@PathVariable Long id) {
        adminService.suspendUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{id}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable Long id) {
        adminService.activateUser(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/providers/pending")
    public ResponseEntity<List<AdminProviderDto>> getPendingProviders() {
        return ResponseEntity.ok(adminService.getPendingProviders());
    }

    @PutMapping("/providers/{id}/verify")
    public ResponseEntity<Void> verifyProvider(@PathVariable Long id) {
        adminService.verifyProvider(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/providers/{id}/reject")
    public ResponseEntity<Void> rejectProvider(@PathVariable Long id) {
        adminService.rejectProvider(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<AdminAppointmentDto>> getAllAppointments() {
        return ResponseEntity.ok(adminService.getAllAppointments());
    }

    @GetMapping("/records")
    public ResponseEntity<List<AdminRecordDto>> getAllRecords() {
        return ResponseEntity.ok(adminService.getAllRecords());
    }
}
