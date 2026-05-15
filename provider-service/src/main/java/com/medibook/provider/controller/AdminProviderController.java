package com.medibook.provider.controller;

import com.medibook.provider.service.ProviderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin provider management endpoints.
 */
@RestController
@RequestMapping("/api/admin/providers")
@RequiredArgsConstructor
@Tag(name = "Admin - Providers", description = "Admin endpoints for provider verification and management")
@SecurityRequirement(name = "bearerAuth")
public class AdminProviderController {

    private final ProviderService providerService;

    @GetMapping
    @Operation(summary = "Get all providers", description = "Retrieves all registered providers regardless of verification status")
    public ResponseEntity<List<Map<String, Object>>> getAllProviders() {
        return ResponseEntity.ok(providerService.getAllProviders());
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending providers", description = "Retrieves providers awaiting admin verification")
    public ResponseEntity<List<Map<String, Object>>> getPendingProviders() {
        return ResponseEntity.ok(providerService.getPendingProviders());
    }

    @PutMapping("/{id}/verify")
    @Operation(summary = "Approve provider", description = "Approves a pending provider for the platform")
    public ResponseEntity<Map<String, String>> approveProvider(@PathVariable Long id) {
        providerService.approveProvider(id);
        return ResponseEntity.ok(Map.of("message", "Provider approved"));
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject provider", description = "Rejects a pending provider application")
    public ResponseEntity<Map<String, String>> rejectProvider(@PathVariable Long id) {
        providerService.rejectProvider(id);
        return ResponseEntity.ok(Map.of("message", "Provider rejected"));
    }
}
