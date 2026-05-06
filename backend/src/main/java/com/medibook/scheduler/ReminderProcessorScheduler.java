package com.medibook.scheduler;

import com.medibook.entity.Appointment;
import com.medibook.entity.Notification;
import com.medibook.entity.ScheduledReminder;
import com.medibook.enums.AppointmentStatus;
import com.medibook.enums.NotificationType;
import com.medibook.enums.RelatedType;
import com.medibook.enums.ReminderStatus;
import com.medibook.repository.AppointmentRepository;
import com.medibook.repository.NotificationRepository;
import com.medibook.repository.ScheduledReminderRepository;
import com.medibook.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled component that processes due reminders.
 * Runs every 5 minutes, picks up PENDING reminders whose scheduledAt <= now,
 * validates appointment status, and dispatches notifications.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderProcessorScheduler {

    private final ScheduledReminderRepository scheduledReminderRepository;
    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    private static final int MAX_RETRIES = 3;

    /**
     * Process due reminders every 5 minutes.
     * Checks appointment status before dispatching to prevent
     * sending reminders for cancelled/completed appointments.
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    @Transactional
    public void processDueReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<ScheduledReminder> dueReminders = scheduledReminderRepository
                .findDueReminders(ReminderStatus.PENDING, now);

        if (dueReminders.isEmpty()) {
            return;
        }

        log.info("Processing {} due reminders", dueReminders.size());

        for (ScheduledReminder reminder : dueReminders) {
            try {
                processReminder(reminder, now);
            } catch (Exception e) {
                log.error("Failed to process reminder id={}: {}", reminder.getId(), e.getMessage(), e);
                reminder.setStatus(ReminderStatus.FAILED);
                reminder.setProcessedAt(now);
                scheduledReminderRepository.save(reminder);
            }
        }
    }

    /**
     * Retry failed email/SMS notifications every 15 minutes.
     * Max 3 retries with logged attempts.
     */
    @Scheduled(fixedRate = 900000) // Every 15 minutes
    @Transactional
    public void retryFailedNotifications() {
        List<Notification> failedNotifications = notificationRepository
                .findByDeliveryStatusAndRetryCountLessThan(ReminderStatus.FAILED, MAX_RETRIES);

        if (failedNotifications.isEmpty()) {
            return;
        }

        log.info("Retrying {} failed notifications", failedNotifications.size());

        for (Notification notification : failedNotifications) {
            try {
                notificationService.retryNotification(notification);
                notification.setDeliveryStatus(ReminderStatus.SENT);
                log.info("Retry successful for notification {}", notification.getNotificationId());
            } catch (Exception e) {
                notification.setRetryCount(notification.getRetryCount() + 1);
                log.warn("Retry #{} failed for notification {}: {}",
                        notification.getRetryCount(), notification.getNotificationId(), e.getMessage());
            }
            notificationRepository.save(notification);
        }
    }

    /**
     * Process a single reminder:
     * 1. Validate appointment still exists and is SCHEDULED
     * 2. Dispatch notification via all channels
     * 3. Mark reminder as SENT or CANCELLED
     */
    private void processReminder(ScheduledReminder reminder, LocalDateTime now) {
        // Fetch and validate appointment
        Appointment appointment = appointmentRepository.findById(reminder.getAppointmentId()).orElse(null);

        if (appointment == null) {
            log.warn("Appointment {} not found for reminder {} — cancelling", reminder.getAppointmentId(), reminder.getId());
            markCancelled(reminder, now);
            return;
        }

        // Only send reminders for SCHEDULED appointments
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            log.info("Appointment {} status is {} — skipping reminder {}",
                    appointment.getId(), appointment.getStatus(), reminder.getId());
            markCancelled(reminder, now);
            return;
        }

        // Build and dispatch notification
        String title = buildTitle(reminder.getReminderType());
        String message = buildMessage(reminder.getReminderType(), appointment);

        notificationService.createAndSendNotification(
                reminder.getRecipientId(),
                reminder.getReminderType(),
                title,
                message,
                reminder.getAppointmentId(),
                reminder.getReminderType() == NotificationType.FOLLOW_UP_REMINDER
                        ? RelatedType.RECORD
                        : RelatedType.APPOINTMENT
        );

        // Mark as sent
        reminder.setStatus(ReminderStatus.SENT);
        reminder.setProcessedAt(now);
        scheduledReminderRepository.save(reminder);

        log.info("Reminder {} dispatched: type={}, appointment={}, recipient={}",
                reminder.getId(), reminder.getReminderType(), reminder.getAppointmentId(), reminder.getRecipientId());
    }

    private void markCancelled(ScheduledReminder reminder, LocalDateTime now) {
        reminder.setStatus(ReminderStatus.CANCELLED);
        reminder.setProcessedAt(now);
        scheduledReminderRepository.save(reminder);
    }

    private String buildTitle(NotificationType type) {
        return switch (type) {
            case REMINDER_24H -> "24 Hour Reminder: Upcoming Appointment";
            case REMINDER_1H -> "1 Hour Reminder: Upcoming Appointment";
            case FOLLOW_UP_REMINDER -> "Follow-Up Reminder";
            default -> "Appointment Reminder";
        };
    }

    private String buildMessage(NotificationType type, Appointment appointment) {
        String patientName = appointment.getPatient().getName();
        String providerName = appointment.getProvider().getUser().getName();
        String date = (appointment.getSlot() != null ? appointment.getSlot().getDate() : appointment.getSlotDate()).toString();
        String time = (appointment.getSlot() != null ? appointment.getSlot().getStartTime() : appointment.getSlotStartTime()).toString();
        String videoLink = "http://localhost:5173/meet/" + appointment.getId();

        return switch (type) {
            case REMINDER_24H -> String.format(
                    "Hello %s, your appointment with Dr. %s is tomorrow at %s. Please be prepared. Join Video Consultation: %s",
                    patientName, providerName, time, videoLink);
            case REMINDER_1H -> String.format(
                    "Hello %s, your appointment with Dr. %s starts in 1 hour at %s. Join Video Consultation: %s",
                    patientName, providerName, time, videoLink);
            case FOLLOW_UP_REMINDER -> String.format(
                    "Hello %s, you have a follow-up scheduled for today regarding your appointment on %s with Dr. %s.",
                    patientName, date, providerName);
            default -> String.format(
                    "Hello %s, you have an upcoming appointment with Dr. %s on %s at %s.",
                    patientName, providerName, date, time);
        };
    }
}
