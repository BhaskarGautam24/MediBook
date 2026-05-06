package com.medibook.service;

import com.medibook.entity.Appointment;
import com.medibook.entity.ScheduledReminder;
import com.medibook.enums.NotificationType;
import com.medibook.enums.ReminderStatus;
import com.medibook.repository.ScheduledReminderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Manages the lifecycle of scheduled reminders.
 * 
 * Responsibilities:
 * - Create 24H and 1H reminders when appointment is confirmed (SCHEDULED)
 * - Create follow-up reminders when medical record has follow-up date
 * - Cancel all pending reminders when appointment is cancelled/rescheduled
 * - Prevent duplicates via idempotency keys
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderSchedulingService {

    private final ScheduledReminderRepository scheduledReminderRepository;

    /**
     * Schedule 24H and 1H reminders for a confirmed appointment.
     * Called when appointment status transitions to SCHEDULED.
     */
    @Transactional
    public void scheduleReminders(Appointment appointment) {
        LocalDateTime appointmentDateTime = LocalDateTime.of(
                appointment.getSlot() != null ? appointment.getSlot().getDate() : appointment.getSlotDate(),
                appointment.getSlot() != null ? appointment.getSlot().getStartTime() : appointment.getSlotStartTime()
        );
        LocalDateTime now = LocalDateTime.now();
        Long patientId = appointment.getPatient().getId();
        Long appointmentId = appointment.getId();

        // Schedule 24-hour reminder
        LocalDateTime reminder24HTime = appointmentDateTime.minusHours(24);
        if (reminder24HTime.isAfter(now)) {
            createReminderIfNotExists(
                    appointmentId,
                    patientId,
                    NotificationType.REMINDER_24H,
                    reminder24HTime,
                    buildIdempotencyKey(NotificationType.REMINDER_24H, appointmentId)
            );
        } else {
            log.debug("Skipping 24H reminder for appointment {} — scheduled time {} is in the past", appointmentId, reminder24HTime);
        }

        // Schedule 1-hour reminder
        LocalDateTime reminder1HTime = appointmentDateTime.minusHours(1);
        if (reminder1HTime.isAfter(now)) {
            createReminderIfNotExists(
                    appointmentId,
                    patientId,
                    NotificationType.REMINDER_1H,
                    reminder1HTime,
                    buildIdempotencyKey(NotificationType.REMINDER_1H, appointmentId)
            );
        } else {
            log.debug("Skipping 1H reminder for appointment {} — scheduled time {} is in the past", appointmentId, reminder1HTime);
        }

        log.info("Reminders scheduled for appointment {} (patient {})", appointmentId, patientId);
    }

    /**
     * Schedule a follow-up reminder based on medical record follow-up date.
     * Reminder is sent at 8:00 AM on the follow-up date.
     */
    @Transactional
    public void scheduleFollowUpReminder(Long recordId, Long patientId, Long appointmentId, LocalDate followUpDate) {
        if (followUpDate == null) {
            return;
        }

        LocalDateTime reminderTime = followUpDate.atTime(8, 0);
        LocalDateTime now = LocalDateTime.now();

        if (reminderTime.isBefore(now)) {
            log.debug("Skipping follow-up reminder for record {} — follow-up date {} is in the past", recordId, followUpDate);
            return;
        }

        String idempotencyKey = "FOLLOW_UP_RECORD_" + recordId;

        // If follow-up date changed, cancel old and create new
        Optional<ScheduledReminder> existing = scheduledReminderRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            ScheduledReminder old = existing.get();
            if (old.getStatus() == ReminderStatus.PENDING) {
                old.setStatus(ReminderStatus.CANCELLED);
                old.setProcessedAt(now);
                scheduledReminderRepository.save(old);
                log.info("Cancelled old follow-up reminder (id={}) for record {}", old.getId(), recordId);
            }
            // Create new with updated key to avoid unique constraint
            idempotencyKey = "FOLLOW_UP_RECORD_" + recordId + "_v" + System.currentTimeMillis();
        }

        createReminderIfNotExists(
                appointmentId,
                patientId,
                NotificationType.FOLLOW_UP_REMINDER,
                reminderTime,
                idempotencyKey
        );

        log.info("Follow-up reminder scheduled for record {} on {}", recordId, followUpDate);
    }

    /**
     * Cancel all pending reminders for an appointment.
     * Called when appointment is cancelled or rescheduled.
     */
    @Transactional
    public void cancelReminders(Long appointmentId) {
        int cancelled = scheduledReminderRepository.cancelByAppointmentId(appointmentId, LocalDateTime.now());
        if (cancelled > 0) {
            log.info("Cancelled {} pending reminders for appointment {}", cancelled, appointmentId);
        }
    }

    /**
     * Creates a ScheduledReminder if the idempotency key doesn't already exist as PENDING.
     */
    private void createReminderIfNotExists(Long appointmentId, Long recipientId,
                                            NotificationType type, LocalDateTime scheduledAt,
                                            String idempotencyKey) {
        Optional<ScheduledReminder> existing = scheduledReminderRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent() && existing.get().getStatus() == ReminderStatus.PENDING) {
            log.debug("Reminder already exists with key {} — skipping", idempotencyKey);
            return;
        }

        ScheduledReminder reminder = ScheduledReminder.builder()
                .appointmentId(appointmentId)
                .recipientId(recipientId)
                .reminderType(type)
                .scheduledAt(scheduledAt)
                .status(ReminderStatus.PENDING)
                .idempotencyKey(idempotencyKey)
                .build();

        scheduledReminderRepository.save(reminder);
        log.debug("Created scheduled reminder: type={}, appointmentId={}, scheduledAt={}", type, appointmentId, scheduledAt);
    }

    private String buildIdempotencyKey(NotificationType type, Long appointmentId) {
        return type.name() + "_APPT_" + appointmentId;
    }
}
