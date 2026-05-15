package com.medibook.schedule.controller;

import com.medibook.schedule.dto.BulkSlotRequest;
import com.medibook.schedule.dto.RecurringSlotRequest;
import com.medibook.schedule.dto.SlotRequest;
import com.medibook.schedule.service.SlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/slots")
@RequiredArgsConstructor
@Tag(name = "Slots", description = "Endpoints for provider slot management")
@SecurityRequirement(name = "bearerAuth")
public class SlotController {

    private final SlotService slotService;

    @PostMapping
    @Operation(summary = "Add a slot", description = "Creates a single availability slot for the provider")
    public ResponseEntity<Map<String, Object>> addSlot(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody SlotRequest request) {
        return ResponseEntity.ok(slotService.addSlot(userId, request));
    }

    @PostMapping("/bulk")
    @Operation(summary = "Create bulk slots", description = "Creates multiple slots at once for a specific date")
    public ResponseEntity<Map<String, Object>> createBulkSlots(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody BulkSlotRequest request) {
        return ResponseEntity.ok(slotService.createBulkSlots(userId, request));
    }

    @PostMapping("/recurring")
    @Operation(summary = "Create recurring slots", description = "Creates slots that repeat on selected days of the week")
    public ResponseEntity<Map<String, Object>> createRecurringSlots(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody RecurringSlotRequest request) {
        return ResponseEntity.ok(slotService.createRecurringSlots(userId, request));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my slots", description = "Retrieves all slots created by the authenticated provider")
    public ResponseEntity<List<Map<String, Object>>> getMySlots(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(slotService.getMySlots(userId));
    }

    @PutMapping("/{id}/block")
    @Operation(summary = "Block a slot", description = "Marks a slot as blocked so it cannot be booked")
    public ResponseEntity<Map<String, Object>> blockSlot(
            @RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        return ResponseEntity.ok(slotService.blockSlot(userId, id));
    }

    @PutMapping("/{id}/unblock")
    @Operation(summary = "Unblock a slot", description = "Removes the block from a slot making it bookable again")
    public ResponseEntity<Map<String, Object>> unblockSlot(
            @RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        return ResponseEntity.ok(slotService.unblockSlot(userId, id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a slot", description = "Permanently removes an unbooked slot")
    public ResponseEntity<Void> deleteSlot(
            @RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        slotService.deleteSlot(userId, id);
        return ResponseEntity.noContent().build();
    }
}
