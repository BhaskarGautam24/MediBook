package com.medibook.schedule.service;

import com.medibook.schedule.client.AppointmentClient;
import com.medibook.schedule.client.ProviderClient;
import com.medibook.schedule.dto.BulkSlotRequest;
import com.medibook.schedule.dto.RecurringSlotRequest;
import com.medibook.schedule.dto.SlotRequest;
import com.medibook.schedule.entity.Slot;
import com.medibook.schedule.enums.RecurrenceType;
import com.medibook.schedule.repository.SlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlotService {

    private final SlotRepository slotRepository;
    private final ProviderClient providerClient;
    private final AppointmentClient appointmentClient;

    // --- Single Slot Creation ---

    @Transactional
    public Map<String, Object> addSlot(Long userId, SlotRequest request) {
        Long providerId = getVerifiedProviderId(userId);

        LocalDate date = LocalDate.parse(request.getDate());
        LocalTime startTime = LocalTime.parse(request.getStartTime());
        LocalTime endTime = LocalTime.parse(request.getEndTime());

        validateTimeRange(startTime, endTime);
        validateNotPastSlot(date, startTime);
        checkOverlap(providerId, date, startTime, endTime);

        Slot slot = Slot.builder()
                .providerId(providerId)
                .date(date)
                .startTime(startTime)
                .endTime(endTime)
                .durationMinutes(request.getDurationMinutes())
                .isBooked(false)
                .isBlocked(false)
                .recurrenceType(RecurrenceType.NONE)
                .build();

        slot = slotRepository.save(slot);
        log.info("Slot created: {} | {} {}-{}", slot.getId(), date, startTime, endTime);
        return toSlotMap(slot);
    }

    // --- Bulk Slot Creation ---

    @Transactional
    public Map<String, Object> createBulkSlots(Long userId, BulkSlotRequest request) {
        Long providerId = getVerifiedProviderId(userId);

        LocalDate startDate = LocalDate.parse(request.getStartDate());
        LocalDate endDate = LocalDate.parse(request.getEndDate());
        LocalTime windowStart = LocalTime.parse(request.getWindowStart());
        LocalTime windowEnd = LocalTime.parse(request.getWindowEnd());
        int duration = request.getDurationMinutes();
        int buffer = request.getBufferMinutes() != null ? request.getBufferMinutes() : 0;

        if (endDate.isBefore(startDate)) throw new RuntimeException("End date must be after start date");
        validateTimeRange(windowStart, windowEnd);

        List<Slot> created = new ArrayList<>();
        int skipped = 0;

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            LocalTime cursor = windowStart;
            while (true) {
                LocalTime slotEnd = cursor.plusMinutes(duration);
                if (slotEnd.isAfter(windowEnd)) break;

                if (slotRepository.findOverlapping(providerId, date, cursor, slotEnd).isEmpty()) {
                    created.add(Slot.builder()
                            .providerId(providerId).date(date).startTime(cursor).endTime(slotEnd)
                            .durationMinutes(duration).isBooked(false).isBlocked(false)
                            .recurrenceType(RecurrenceType.NONE).build());
                } else {
                    skipped++;
                }
                cursor = slotEnd.plusMinutes(buffer);
                if (cursor.isBefore(slotEnd)) break;
            }
        }

        slotRepository.saveAll(created);
        log.info("Bulk slots created: {} | Skipped: {}", created.size(), skipped);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("created", created.size());
        result.put("skipped", skipped);
        result.put("slots", created.stream().map(this::toSlotMap).toList());
        return result;
    }

    // --- Recurring Slot Creation ---

    @Transactional
    public Map<String, Object> createRecurringSlots(Long userId, RecurringSlotRequest request) {
        Long providerId = getVerifiedProviderId(userId);

        RecurrenceType recurrence = RecurrenceType.valueOf(request.getRecurrenceType().toUpperCase());
        LocalDate startDate = LocalDate.parse(request.getStartDate());
        LocalDate endDate = LocalDate.parse(request.getEndDate());
        LocalTime startTime = LocalTime.parse(request.getStartTime());
        LocalTime endTime = LocalTime.parse(request.getEndTime());

        validateTimeRange(startTime, endTime);
        if (endDate.isBefore(startDate)) throw new RuntimeException("End date must be after start date");

        DayOfWeek targetDay = startDate.getDayOfWeek();
        List<Slot> created = new ArrayList<>();
        int skipped = 0;

        for (LocalDate date = startDate; !date.isAfter(endDate);
             date = recurrence == RecurrenceType.WEEKLY ? date.plusWeeks(1) : date.plusDays(1)) {
            if (recurrence == RecurrenceType.WEEKLY && date.getDayOfWeek() != targetDay) continue;
            if (slotRepository.findOverlapping(providerId, date, startTime, endTime).isEmpty()) {
                created.add(Slot.builder().providerId(providerId).date(date)
                        .startTime(startTime).endTime(endTime)
                        .isBooked(false).isBlocked(false).recurrenceType(recurrence).build());
            } else {
                skipped++;
            }
        }

        slotRepository.saveAll(created);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("recurrenceType", recurrence.name());
        result.put("created", created.size());
        result.put("skipped", skipped);
        result.put("slots", created.stream().map(this::toSlotMap).toList());
        return result;
    }

    // --- Slot Queries ---

    public List<Map<String, Object>> getMySlots(Long userId) {
        Long providerId = getProviderId(userId);
        return slotRepository.findByProviderIdOrderByDateAscStartTimeAsc(providerId)
                .stream().map(this::toSlotMap).toList();
    }

    public List<Map<String, Object>> getSlotsByProviderAndDate(Long providerId, String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        return slotRepository.findAvailableSlots(providerId, date)
                .stream().map(this::toSlotMap).toList();
    }

    public List<Map<String, Object>> getAvailableSlotsForProvider(Long providerId) {
        return slotRepository.findAvailableSlotsFrom(providerId, LocalDate.now())
                .stream().map(this::toSlotMap).toList();
    }

    // --- Block / Unblock ---

    @Transactional
    public Map<String, Object> blockSlot(Long userId, Long slotId) {
        Slot slot = getOwnSlot(userId, slotId);
        if (Boolean.TRUE.equals(slot.getIsBooked())) {
            throw new RuntimeException("Cannot block a booked slot — cancel the appointment first");
        }
        slot.setIsBlocked(true);
        return toSlotMap(slotRepository.save(slot));
    }

    @Transactional
    public Map<String, Object> unblockSlot(Long userId, Long slotId) {
        Slot slot = getOwnSlot(userId, slotId);
        slot.setIsBlocked(false);
        return toSlotMap(slotRepository.save(slot));
    }

    // --- Delete Slot ---

    @Transactional
    public void deleteSlot(Long userId, Long slotId) {
        Slot slot = getOwnSlot(userId, slotId);
        // Notify appointment-service to handle any linked appointments
        try {
            appointmentClient.handleSlotExpired(slotId);
        } catch (Exception e) {
            log.warn("Failed to notify appointment-service for slot deletion: {}", e.getMessage());
        }
        slotRepository.delete(slot);
        log.info("Slot {} deleted by provider", slotId);
    }

    // --- Expired Slot Cleanup ---

    @Transactional
    public void deleteExpiredSlot(Long slotId) {
        Slot slot = slotRepository.findById(slotId).orElse(null);
        if (slot == null) return;

        try {
            appointmentClient.handleSlotExpired(slotId);
        } catch (Exception e) {
            log.warn("Failed to notify appointment-service for expired slot {}: {}", slotId, e.getMessage());
        }

        slotRepository.deleteSlotById(slotId);
        log.info("Expired slot {} deleted", slotId);
    }

    public List<Slot> findExpiredSlots() {
        return slotRepository.findExpiredSlots(LocalDate.now(), LocalTime.now());
    }

    // --- Internal: update booking status ---

    @Transactional
    public void markSlotBooked(Long slotId, boolean booked) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));
        slot.setIsBooked(booked);
        slotRepository.save(slot);
    }

    // --- Private Helpers ---

    private Long getVerifiedProviderId(Long userId) {
        Map<String, Object> provider = providerClient.getProviderByUserId(userId);
        if (provider == null) throw new RuntimeException("Provider profile not found");
        Boolean isVerified = (Boolean) provider.get("isVerified");
        if (!Boolean.TRUE.equals(isVerified)) {
            throw new RuntimeException("Your profile must be verified before adding slots");
        }
        return Long.valueOf(provider.get("id").toString());
    }

    private Long getProviderId(Long userId) {
        Map<String, Object> provider = providerClient.getProviderByUserId(userId);
        if (provider == null) throw new RuntimeException("Provider profile not found");
        return Long.valueOf(provider.get("id").toString());
    }

    private Slot getOwnSlot(Long userId, Long slotId) {
        Long providerId = getProviderId(userId);
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));
        if (!slot.getProviderId().equals(providerId)) {
            throw new RuntimeException("You can only manage your own slots");
        }
        return slot;
    }

    private void validateTimeRange(LocalTime start, LocalTime end) {
        if (!end.isAfter(start)) throw new RuntimeException("End time must be after start time");
    }

    private void validateNotPastSlot(LocalDate date, LocalTime time) {
        if (date.isBefore(LocalDate.now())) throw new RuntimeException("Cannot create slots in the past");
        if (date.equals(LocalDate.now()) && time.isBefore(LocalTime.now()))
            throw new RuntimeException("Cannot create slots in the past");
    }

    private void checkOverlap(Long providerId, LocalDate date, LocalTime start, LocalTime end) {
        if (!slotRepository.findOverlapping(providerId, date, start, end).isEmpty()) {
            throw new RuntimeException("This slot overlaps with an existing slot on the same day");
        }
    }

    public Map<String, Object> toSlotMap(Slot slot) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", slot.getId());
        map.put("providerId", slot.getProviderId());
        map.put("date", slot.getDate().toString());
        map.put("startTime", slot.getStartTime().toString());
        map.put("endTime", slot.getEndTime().toString());
        map.put("durationMinutes", slot.getDurationMinutes());
        map.put("booked", Boolean.TRUE.equals(slot.getIsBooked()));
        map.put("blocked", Boolean.TRUE.equals(slot.getIsBlocked()));
        map.put("recurrenceType", slot.getRecurrenceType() != null ? slot.getRecurrenceType().name() : "NONE");
        map.put("createdAt", slot.getCreatedAt());
        return map;
    }
}
