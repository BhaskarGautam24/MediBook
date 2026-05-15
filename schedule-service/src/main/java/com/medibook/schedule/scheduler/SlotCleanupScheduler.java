package com.medibook.schedule.scheduler;

import com.medibook.schedule.entity.Slot;
import com.medibook.schedule.service.SlotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Cleans up expired slots every minute.
 * Delegates appointment handling to appointment-service before deleting slot.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SlotCleanupScheduler {

    private final SlotService slotService;

    @Scheduled(fixedRate = 60000) // Every 1 minute
    public void cleanupExpiredSlots() {
        List<Slot> expired = slotService.findExpiredSlots();
        if (expired.isEmpty()) return;

        log.info("Found {} expired slots to clean up", expired.size());
        for (Slot slot : expired) {
            try {
                slotService.deleteExpiredSlot(slot.getId());
            } catch (Exception e) {
                log.error("Failed to clean up slot {}: {}", slot.getId(), e.getMessage());
            }
        }
    }
}
