package com.medibook.scheduler;

import com.medibook.entity.Slot;
import com.medibook.repository.SlotRepository;
import com.medibook.service.SlotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Scheduled component that cleans up expired slots.
 * Runs every 60 seconds. Each slot is processed in its own transaction
 * via SlotService to ensure proper commit/rollback isolation.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SlotCleanupScheduler {

    private final SlotRepository slotRepository;
    private final SlotService slotService;

    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredSlots() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        List<Slot> expiredSlots = slotRepository.findExpiredSlots(today, now);

        if (expiredSlots.isEmpty()) {
            return;
        }

        log.info("Found {} expired slots. Removing them automatically...", expiredSlots.size());

        for (Slot slot : expiredSlots) {
            try {
                slotService.deleteExpiredSlot(slot.getId());
            } catch (Exception e) {
                log.error("Failed to cleanup expired slot {}: {}", slot.getId(), e.getMessage(), e);
            }
        }
    }
}
