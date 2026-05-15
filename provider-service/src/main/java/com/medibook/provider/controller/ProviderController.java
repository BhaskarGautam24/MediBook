package com.medibook.provider.controller;

import com.medibook.provider.dto.ProviderProfileUpdateRequest;
import com.medibook.provider.service.CloudinaryService;
import com.medibook.provider.service.ProviderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * ProviderController — same API contract as monolith.
 * Uses X-User-Id header (set by API Gateway) instead of @AuthenticationPrincipal.
 */
@RestController
@RequestMapping("/api/providers")
@RequiredArgsConstructor
@Tag(name = "Providers", description = "Endpoints for provider profiles and public listings")
public class ProviderController {

    private final ProviderService providerService;
    private final CloudinaryService cloudinaryService;

    // ── Public Endpoints ──

    @GetMapping
    @Operation(summary = "Get verified providers", description = "Retrieves a list of all verified healthcare providers")
    public ResponseEntity<List<Map<String, Object>>> getVerifiedProviders() {
        return ResponseEntity.ok(providerService.getVerifiedProviders());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get provider by ID", description = "Retrieves a specific provider's public profile")
    public ResponseEntity<Map<String, Object>> getProvider(@PathVariable Long id) {
        return ResponseEntity.ok(providerService.getProviderById(id));
    }

    // ── Provider-Only Endpoints (userId from gateway header) ──

    @GetMapping("/me/stats")
    @Operation(summary = "Get my dashboard stats", description = "Retrieves statistics for the authenticated provider")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, Object>> getMyStats(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(providerService.getProviderStats(userId));
    }

    @GetMapping("/me/profile")
    @Operation(summary = "Get my profile", description = "Retrieves the authenticated provider's full profile")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, Object>> getMyProfile(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(providerService.getMyProfile(userId));
    }

    @PutMapping("/me/profile")
    @Operation(summary = "Update my profile", description = "Updates the authenticated provider's profile information")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, Object>> updateMyProfile(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ProviderProfileUpdateRequest request) {
        return ResponseEntity.ok(providerService.updateMyProfile(userId, request));
    }

    @PostMapping("/me/picture")
    @Operation(summary = "Upload profile picture", description = "Uploads a new profile picture for the provider")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, String>> uploadProfilePicture(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam("file") MultipartFile file) {
        String imageUrl = cloudinaryService.uploadImage(file, "medibook/providers");
        String oldUrl = providerService.updateProfilePicture(userId, imageUrl);
        if (oldUrl != null && oldUrl.contains("cloudinary")) {
            cloudinaryService.deleteImage(oldUrl);
        }
        return ResponseEntity.ok(Map.of("message", "Profile picture updated", "profilePicture", imageUrl));
    }

    @PutMapping("/me/fee")
    @Operation(summary = "Update consultation fee", description = "Updates the provider's consultation fee")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, Object>> updateConsultationFee(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Map<String, Object> body) {
        Double fee = Double.valueOf(body.get("consultationFee").toString());
        return ResponseEntity.ok(providerService.updateConsultationFee(userId, fee));
    }
}
