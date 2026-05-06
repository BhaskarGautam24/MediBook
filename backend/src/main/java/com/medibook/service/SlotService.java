package com.medibook.service;

import com.medibook.dto.BulkSlotRequest;
import com.medibook.dto.RecurringSlotRequest;
import com.medibook.dto.SlotRequest;
import com.medibook.entity.Appointment;
import com.medibook.entity.MedicalRecord;
import com.medibook.entity.Provider;
import com.medibook.entity.Slot;
import com.medibook.enums.AppointmentStatus;
import com.medibook.enums.NotificationType;
import com.medibook.enums.RecurrenceType;
import com.medibook.enums.RelatedType;
import com.medibook.repository.AppointmentRepository;
import com.medibook.repository.ProviderRepository;
import com.medibook.repository.RecordRepository;
import com.medibook.repository.SlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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
    private final ProviderRepository providerRepository;
    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;
    private final RecordRepository recordRepository;
    private final ReminderSchedulingService reminderSchedulingService;

    // ────────────────────────────────────────────────────────────
    // Single Slot Creation
    // ────────────────────────────────────────────────────────────

    @Transactional
    public Map<String, Object> addSlot(Long userId, SlotRequest request) {
        Provider provider = getVerifiedProvider(userId);

        LocalDate date = LocalDate.parse(request.getDate());
        LocalTime startTime = LocalTime.parse(request.getStartTime());
        LocalTime endTime = LocalTime.parse(request.getEndTime());

        validateTimeRange(startTime, endTime);
        validateNotPastSlot(date, startTime);
        checkOverlap(provider.getId(), date, startTime, endTime);

        Slot slot = Slot.builder()
                .provider(provider)
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

    // ────────────────────────────────────────────────────────────
    // Bulk Slot Creation
    // ────────────────────────────────────────────────────────────

    @Transactional
    public Map<String, Object> createBulkSlots(Long userId, BulkSlotRequest request) {
        Provider provider = getVerifiedProvider(userId);

        LocalDate startDate = LocalDate.parse(request.getStartDate());
        LocalDate endDate = LocalDate.parse(request.getEndDate());
        LocalTime windowStart = LocalTime.parse(request.getWindowStart());
        LocalTime windowEnd = LocalTime.parse(request.getWindowEnd());
        int duration = request.getDurationMinutes();
        int buffer = request.getBufferMinutes() != null ? request.getBufferMinutes() : 0;

        if (endDate.isBefore(startDate)) {
            throw new RuntimeException("End date must be after start date");
        }
        validateTimeRange(windowStart, windowEnd);

        List<Slot> created = new ArrayList<>();
        int skipped = 0;

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            LocalTime cursor = windowStart;

            while (true) {
                LocalTime slotEnd = cursor.plusMinutes(duration);
                if (slotEnd.isAfter(windowEnd)) break;

                // Skip if overlaps with existing
                if (slotRepository.findOverlapping(provider.getId(), date, cursor, slotEnd).isEmpty()) {
                    Slot slot = Slot.builder()
                            .provider(provider)
                            .date(date)
                            .startTime(cursor)
                            .endTime(slotEnd)
                            .durationMinutes(duration)
                            .isBooked(false)
                            .isBlocked(false)
                            .recurrenceType(RecurrenceType.NONE)
                            .build();
                    created.add(slot);
                } else {
                    skipped++;
                }

                cursor = slotEnd.plusMinutes(buffer);
                // Guard against time wrapping past midnight
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

    // ────────────────────────────────────────────────────────────
    // Recurring Slot Creation
    // ────────────────────────────────────────────────────────────

    @Transactional
    public Map<String, Object> createRecurringSlots(Long userId, RecurringSlotRequest request) {
        Provider provider = getVerifiedProvider(userId);

        RecurrenceType recurrence = RecurrenceType.valueOf(request.getRecurrenceType().toUpperCase());
        LocalDate startDate = LocalDate.parse(request.getStartDate());
        LocalDate endDate = LocalDate.parse(request.getEndDate());
        LocalTime startTime = LocalTime.parse(request.getStartTime());
        LocalTime endTime = LocalTime.parse(request.getEndTime());

        validateTimeRange(startTime, endTime);
        if (endDate.isBefore(startDate)) {
            throw new RuntimeException("End date must be after start date");
        }

        DayOfWeek targetDay = startDate.getDayOfWeek();
        List<Slot> created = new ArrayList<>();
        int skipped = 0;

        for (LocalDate date = startDate; !date.isAfter(endDate);
             date = recurrence == RecurrenceType.WEEKLY ? date.plusWeeks(1) : date.plusDays(1)) {

            // For WEEKLY: only create on same day-of-week
            if (recurrence == RecurrenceType.WEEKLY && date.getDayOfWeek() != targetDay) {
                continue;
            }

            if (slotRepository.findOverlapping(provider.getId(), date, startTime, endTime).isEmpty()) {
                Slot slot = Slot.builder()
                        .provider(provider)
                        .date(date)
                        .startTime(startTime)
                        .endTime(endTime)
                        .isBooked(false)
                        .isBlocked(false)
                        .recurrenceType(recurrence)
                        .build();
                created.add(slot);
            } else {
                skipped++;
            }
        }

        slotRepository.saveAll(created);
        log.info("Recurring ({}) slots created: {} | Skipped: {}", recurrence, created.size(), skipped);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("recurrenceType", recurrence.name());
        result.put("created", created.size());
        result.put("skipped", skipped);
        result.put("slots", created.stream().map(this::toSlotMap).toList());
        return result;
    }

    // ────────────────────────────────────────────────────────────
    // Slot Queries
    // ────────────────────────────────────────────────────────────

    /** Provider's own view — all their slots, excludes completed appointments */
    public List<Map<String, Object>> getMySlots(Long userId) {
        Provider provider = providerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Provider profile not found"));

        return slotRepository.findByProviderIdOrderByDateAscStartTimeAsc(provider.getId())
                .stream()
                .filter(slot -> {
                    List<Appointment> appts = appointmentRepository.findBySlotId(slot.getId());
                    return appts.stream().noneMatch(a -> a.getStatus() == AppointmentStatus.COMPLETED);
                })
                .map(this::toSlotMap)
                .toList();
    }

    /** Patient view: date-based, hides blocked + completed slots */
    public List<Map<String, Object>> getSlotsByProviderAndDate(Long providerId, String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        return slotRepository.findAvailableSlots(providerId, date)
                .stream()
                .filter(slot -> {
                    List<Appointment> appts = appointmentRepository.findBySlotId(slot.getId());
                    return appts.stream().noneMatch(a -> a.getStatus() == AppointmentStatus.COMPLETED);
                })
                .map(this::toSlotMap)
                .toList();
    }

    /** Patient calendar view: all available slots from today forward */
    public List<Map<String, Object>> getAvailableSlotsForProvider(Long providerId) {
        return slotRepository.findAvailableSlotsFrom(providerId, LocalDate.now())
                .stream()
                .filter(slot -> {
                    List<Appointment> appts = appointmentRepository.findBySlotId(slot.getId());
                    return appts.stream().noneMatch(a -> a.getStatus() == AppointmentStatus.COMPLETED);
                })
                .map(this::toSlotMap)
                .toList();
    }

    // ────────────────────────────────────────────────────────────
    // Block / Unblock
    // ────────────────────────────────────────────────────────────

    @Transactional
    public Map<String, Object> blockSlot(Long userId, Long slotId) {
        Slot slot = getOwnSlot(userId, slotId);

        if (Boolean.TRUE.equals(slot.getIsBooked())) {
            throw new RuntimeException("Cannot block a booked slot — cancel the appointment first");
        }

        slot.setIsBlocked(true);
        slot = slotRepository.save(slot);
        log.info("Slot {} blocked", slotId);
        return toSlotMap(slot);
    }

    @Transactional
    public Map<String, Object> unblockSlot(Long userId, Long slotId) {
        Slot slot = getOwnSlot(userId, slotId);
        slot.setIsBlocked(false);
        slot = slotRepository.save(slot);
        log.info("Slot {} unblocked", slotId);
        return toSlotMap(slot);
    }

    // ────────────────────────────────────────────────────────────
    // Delete Slot (with appointment cleanup)
    // ────────────────────────────────────────────────────────────

    @Transactional
    public void deleteSlot(Long userId, Long slotId) {
        Slot slot = getOwnSlot(userId, slotId);

        List<Appointment> appointments = appointmentRepository.findBySlotId(slotId);

        for (Appointment appt : appointments) {
            if (appt.getStatus() == AppointmentStatus.PENDING
                    || appt.getStatus() == AppointmentStatus.BOOKED
                    || appt.getStatus() == AppointmentStatus.SCHEDULED) {

                appt.setStatus(AppointmentStatus.CANCELLED);
                appointmentRepository.save(appt);

                String dateTime = slot.getDate() + " at " + slot.getStartTime();
                String message = "We sincerely apologize — your booked slot on " + dateTime
                        + " had to be cancelled. We understand this may be inconvenient. "
                        + "Please choose a new time that works best for you.";

                notificationService.createAndSendNotification(
                        appt.getPatient().getId(),
                        NotificationType.APPOINTMENT_CANCELLED,
                        "Appointment Cancelled",
                        message,
                        appt.getId(),
                        RelatedType.APPOINTMENT
                );
            }
        }

        // Delete medical records first to avoid FK constraint violation
        for (Appointment appt : appointments) {
            recordRepository.findByAppointmentId(appt.getId())
                    .ifPresent(recordRepository::delete);
        }
        appointmentRepository.deleteAll(appointments);
        slotRepository.delete(slot);
        log.info("Slot {} deleted with {} appointments cleaned", slotId, appointments.size());
    }

    @Transactional
    public void deleteExpiredSlot(Long slotId) {
        Slot slot = slotRepository.findById(slotId).orElse(null);
        if (slot != null) {
            deleteSlotSystem(slot);
        }
    }

    private void deleteSlotSystem(Slot slot) {
        List<Appointment> appointments = appointmentRepository.findBySlotId(slot.getId());

        for (Appointment appt : appointments) {
            switch (appt.getStatus()) {

                // ── Case 1: Doctor accepted but didn't click "Complete" ──
                // Auto-complete the appointment. Data preserved for both sides.
                case SCHEDULED -> {
                    appt.setStatus(AppointmentStatus.COMPLETED);
                    appt.setSlot(null);
                    appointmentRepository.save(appt);

                    // Cancel any remaining reminders
                    try { reminderSchedulingService.cancelReminders(appt.getId()); } catch (Exception ignored) {}

                    String dateTime = slot.getDate() + " at " + slot.getStartTime() + " - " + slot.getEndTime();
                    notificationService.createAndSendNotification(
                            appt.getPatient().getId(),
                            NotificationType.APPOINTMENT_COMPLETED,
                            "Appointment Auto-Completed",
                            "Your appointment with Dr. " + appt.getProvider().getUser().getName()
                                    + " on " + dateTime + " has been automatically marked as completed.",
                            appt.getId(),
                            RelatedType.APPOINTMENT
                    );

                    log.info("Auto-completed SCHEDULED appointment {} (slot {} expired)", appt.getId(), slot.getId());
                }

                // ── Case 2: Patient booked but doctor never accepted ──
                // Cancel with polite apology. Appointment record preserved so patient sees it.
                case PENDING, BOOKED -> {
                    appt.setStatus(AppointmentStatus.CANCELLED);
                    appt.setSlot(null);
                    appointmentRepository.save(appt);

                    // Cancel any pending reminders
                    try { reminderSchedulingService.cancelReminders(appt.getId()); } catch (Exception ignored) {}

                    String dateTime2 = slot.getDate() + " at " + slot.getStartTime() + " - " + slot.getEndTime();
                    notificationService.createAndSendNotification(
                            appt.getPatient().getId(),
                            NotificationType.APPOINTMENT_CANCELLED,
                            "Appointment Expired — We Apologize",
                            "We sincerely apologize. Your appointment request for " + dateTime2
                                    + " could not be confirmed as the provider was unable to respond in time. "
                                    + "Please book another available slot at your convenience. We're sorry for the inconvenience.",
                            appt.getId(),
                            RelatedType.APPOINTMENT
                    );

                    log.info("Cancelled unconfirmed appointment {} with apology (slot {} expired)", appt.getId(), slot.getId());
                }

                // ── Case 3: Already completed by doctor ──
                // Just detach from slot. All data (appointment + medical record) stays.
                case COMPLETED -> {
                    appt.setSlot(null);
                    appointmentRepository.save(appt);
                    log.info("Preserved completed appointment {} (detached from expired slot {})", appt.getId(), slot.getId());
                }

                // ── Case 4: Already cancelled/rejected — just detach ──
                case CANCELLED, REJECTED, NO_SHOW -> {
                    appt.setSlot(null);
                    appointmentRepository.save(appt);
                    log.info("Detached {} appointment {} from expired slot {}", appt.getStatus(), appt.getId(), slot.getId());
                }
            }
        }

        // All appointments detached — safe to delete the slot
        slotRepository.deleteSlotById(slot.getId());
        log.info("Expired slot {} deleted | {} appointments preserved", slot.getId(), appointments.size());
    }

    // ────────────────────────────────────────────────────────────
    // Private Helpers
    // ────────────────────────────────────────────────────────────

    private Provider getVerifiedProvider(Long userId) {
        Provider provider = providerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Provider profile not found"));

        if (!provider.getIsVerified()) {
            throw new RuntimeException("Your profile must be verified before adding slots");
        }
        return provider;
    }

    private Slot getOwnSlot(Long userId, Long slotId) {
        Provider provider = providerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Provider profile not found"));

        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        if (!slot.getProvider().getId().equals(provider.getId())) {
            throw new RuntimeException("You can only manage your own slots");
        }
        return slot;
    }

    private void validateTimeRange(LocalTime start, LocalTime end) {
        if (!end.isAfter(start)) {
            throw new RuntimeException("End time must be after start time");
        }
    }

    private void validateNotPastSlot(LocalDate date, LocalTime time) {
        if (date.isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot create slots in the past");
        }
        if (date.equals(LocalDate.now()) && time.isBefore(LocalTime.now())) {
            throw new RuntimeException("Cannot create slots in the past");
        }
    }

    private void checkOverlap(Long providerId, LocalDate date, LocalTime start, LocalTime end) {
        List<Slot> overlapping = slotRepository.findOverlapping(providerId, date, start, end);
        if (!overlapping.isEmpty()) {
            throw new RuntimeException("This slot overlaps with an existing slot on the same day");
        }
    }

    Map<String, Object> toSlotMap(Slot slot) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", slot.getId());
        map.put("providerId", slot.getProvider().getId());
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
