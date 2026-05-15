package com.medibook.schedule.controller;

import com.medibook.schedule.entity.Slot;
import com.medibook.schedule.repository.SlotRepository;
import com.medibook.schedule.service.SlotService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Handles both:
 * 1. Provider slot endpoints routed from gateway (/api/providers/{id}/slots)
 * 2. Internal service-to-service endpoints (/api/internal/slots/*)
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Provider Slots", description = "Public provider availability slot lookup")
public class InternalSlotController {

    private final SlotService slotService;
    private final SlotRepository slotRepository;

    // --- Provider slot endpoints (routed from gateway) ---

    @GetMapping("/api/providers/{id}/slots")
    @Operation(summary = "Get provider slots by date", description = "Retrieves available slots for a provider on a specific date")
    public ResponseEntity<List<Map<String, Object>>> getProviderSlots(
            @PathVariable Long id, @RequestParam String date) {
        return ResponseEntity.ok(slotService.getSlotsByProviderAndDate(id, date));
    }

    @GetMapping("/api/providers/{id}/slots/all")
    @Operation(summary = "Get all provider slots", description = "Retrieves all available future slots for a provider")
    public ResponseEntity<List<Map<String, Object>>> getAllProviderSlots(@PathVariable Long id) {
        return ResponseEntity.ok(slotService.getAvailableSlotsForProvider(id));
    }

    // --- Internal service-to-service endpoints ---

    @Hidden
    @GetMapping("/api/internal/slots/{id}")
    public ResponseEntity<Map<String, Object>> getSlotById(@PathVariable Long id) {
        Slot slot = slotRepository.findById(id).orElse(null);
        if (slot == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(slotService.toSlotMap(slot));
    }

    @Hidden
    @PutMapping("/api/internal/slots/{id}/booking")
    public ResponseEntity<Map<String, String>> updateBookingStatus(
            @PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        slotService.markSlotBooked(id, body.getOrDefault("booked", false));
        return ResponseEntity.ok(Map.of("message", "Slot booking status updated"));
    }

    @Hidden
    @GetMapping("/api/internal/slots/stats/{providerId}")
    public ResponseEntity<Map<String, Object>> getSlotStats(@PathVariable Long providerId) {
        long total = slotRepository.countByProviderId(providerId);
        long booked = slotRepository.countByProviderIdAndIsBookedTrue(providerId);
        return ResponseEntity.ok(Map.of("totalSlots", total, "bookedSlots", booked));
    }
}
