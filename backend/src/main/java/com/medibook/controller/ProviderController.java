package com.medibook.controller;

import com.medibook.entity.User;
import com.medibook.service.ProviderService;
import com.medibook.service.SlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/providers")
@RequiredArgsConstructor
public class ProviderController {

    private final ProviderService providerService;
    private final SlotService slotService;

    /**
     * GET /api/providers — List all verified providers (public)
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getVerifiedProviders() {
        return ResponseEntity.ok(providerService.getVerifiedProviders());
    }

    /**
     * GET /api/providers/{id} — Get provider details (public)
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProvider(@PathVariable Long id) {
        return ResponseEntity.ok(providerService.getProviderById(id));
    }

    /**
     * GET /api/providers/{id}/slots?date=YYYY-MM-DD — Get slots for a provider on a date (public)
     */
    @GetMapping("/{id}/slots")
    public ResponseEntity<List<Map<String, Object>>> getProviderSlots(
            @PathVariable Long id,
            @RequestParam String date) {
        return ResponseEntity.ok(slotService.getSlotsByProviderAndDate(id, date));
    }

    /**
     * GET /api/providers/{id}/slots/all — Get all available slots from today (patient calendar)
     */
    @GetMapping("/{id}/slots/all")
    public ResponseEntity<List<Map<String, Object>>> getAllProviderSlots(@PathVariable Long id) {
        return ResponseEntity.ok(slotService.getAvailableSlotsForProvider(id));
    }

    /**
     * GET /api/providers/me/stats — Provider dashboard stats (provider only)
     */
    @GetMapping("/me/stats")
    public ResponseEntity<Map<String, Object>> getMyStats(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(providerService.getProviderStats(user.getId()));
    }

    /**
     * PUT /api/providers/me/fee — Update consultation fee (provider only)
     * Body: { "consultationFee": 800 }
     */
    @PutMapping("/me/fee")
    public ResponseEntity<Map<String, Object>> updateConsultationFee(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, Object> body) {
        Double fee = Double.valueOf(body.get("consultationFee").toString());
        return ResponseEntity.ok(providerService.updateConsultationFee(user.getId(), fee));
    }
}
