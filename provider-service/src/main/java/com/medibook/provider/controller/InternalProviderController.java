package com.medibook.provider.controller;

import com.medibook.provider.entity.Provider;
import com.medibook.provider.enums.ProviderStatus;
import com.medibook.provider.repository.ProviderRepository;
import com.medibook.provider.service.ProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Hidden;

import java.util.Map;

/**
 * Internal API for service-to-service communication.
 * Not exposed through API Gateway to public.
 */
@Hidden
@RestController
@RequestMapping("/api/internal/providers")
@RequiredArgsConstructor
public class InternalProviderController {

    private final ProviderRepository providerRepository;
    private final ProviderService providerService;

    /** Called by auth-service during PROVIDER registration */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createProvider(@RequestBody Map<String, Object> data) {
        Provider provider = Provider.builder()
                .userId(Long.valueOf(data.get("userId").toString()))
                .specialization((String) data.get("specialization"))
                .experienceYears(data.get("experienceYears") != null
                        ? Integer.valueOf(data.get("experienceYears").toString()) : null)
                .clinicName((String) data.get("clinicName"))
                .clinicAddress((String) data.get("clinicAddress"))
                .isVerified(false)
                .status(ProviderStatus.PENDING)
                .build();
        provider = providerRepository.save(provider);
        return ResponseEntity.ok(Map.of("created", true, "providerId", provider.getId()));
    }

    /** Get provider by ID */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProviderById(@PathVariable Long id) {
        return ResponseEntity.ok(providerService.getProviderById(id));
    }

    /** Get provider by userId */
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<Map<String, Object>> getProviderByUserId(@PathVariable Long userId) {
        Provider provider = providerRepository.findByUserId(userId).orElse(null);
        if (provider == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(providerService.getProviderById(provider.getId()));
    }

    /** Update provider rating (called by review-service) */
    @PutMapping("/{id}/rating")
    public ResponseEntity<Map<String, String>> updateRating(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Double averageRating = Double.valueOf(body.get("averageRating").toString());
        Integer totalReviews = Integer.valueOf(body.get("totalReviews").toString());
        providerService.updateRating(id, averageRating, totalReviews);
        return ResponseEntity.ok(Map.of("message", "Rating updated"));
    }
}
