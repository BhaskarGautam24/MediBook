package com.medibook.controller;

import com.medibook.dto.BulkSlotRequest;
import com.medibook.dto.RecurringSlotRequest;
import com.medibook.dto.SlotRequest;
import com.medibook.entity.User;
import com.medibook.service.SlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/slots")
@RequiredArgsConstructor
public class SlotController {

    private final SlotService slotService;

    // ── Single Slot Creation ────────────────────────────────────

    @PostMapping
    public ResponseEntity<Map<String, Object>> addSlot(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody SlotRequest request) {
        return ResponseEntity.ok(slotService.addSlot(user.getId(), request));
    }

    // ── Bulk Slot Creation ──────────────────────────────────────

    @PostMapping("/bulk")
    public ResponseEntity<Map<String, Object>> createBulkSlots(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody BulkSlotRequest request) {
        return ResponseEntity.ok(slotService.createBulkSlots(user.getId(), request));
    }

    // ── Recurring Slot Creation ─────────────────────────────────

    @PostMapping("/recurring")
    public ResponseEntity<Map<String, Object>> createRecurringSlots(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody RecurringSlotRequest request) {
        return ResponseEntity.ok(slotService.createRecurringSlots(user.getId(), request));
    }

    // ── Get My Slots (Provider View) ────────────────────────────

    @GetMapping("/my")
    public ResponseEntity<List<Map<String, Object>>> getMySlots(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(slotService.getMySlots(user.getId()));
    }

    // ── Block Slot ──────────────────────────────────────────────

    @PutMapping("/{id}/block")
    public ResponseEntity<Map<String, Object>> blockSlot(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(slotService.blockSlot(user.getId(), id));
    }

    // ── Unblock Slot ────────────────────────────────────────────

    @PutMapping("/{id}/unblock")
    public ResponseEntity<Map<String, Object>> unblockSlot(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(slotService.unblockSlot(user.getId(), id));
    }

    // ── Delete Slot ─────────────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSlot(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        slotService.deleteSlot(user.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
