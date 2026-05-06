package com.medibook.service.impl;

import com.medibook.dto.NotificationDTO;
import com.medibook.entity.Notification;
import com.medibook.entity.User;
import com.medibook.enums.NotificationChannel;
import com.medibook.enums.NotificationType;
import com.medibook.enums.RelatedType;
import com.medibook.enums.ReminderStatus;
import com.medibook.repository.NotificationRepository;
import com.medibook.repository.UserRepository;
import com.medibook.service.EmailService;
import com.medibook.service.NotificationService;
import com.medibook.service.SmsService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Notification service with multi-channel dispatch (In-App, Email, SMS).
 *
 * Reminder scheduling logic has been extracted to:
 * - {@link com.medibook.service.ReminderSchedulingService} — creates/cancels scheduled reminders
 * - {@link com.medibook.scheduler.ReminderProcessorScheduler} — processes due reminders on a cron
 *
 * This service focuses on:
 * 1. CRUD operations for in-app notifications
 * 2. Multi-channel notification dispatch
 * 3. Retry support for failed deliveries
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final UserRepository userRepository;

    // ────────────────────────────────────────────────────────────
    // CRUD Operations (In-App)
    // ────────────────────────────────────────────────────────────

    @Override
    public List<NotificationDTO> getUserNotifications(Long userId) {
        return notificationRepository.findByRecipientIdAndChannelOrderBySentAtDesc(userId, NotificationChannel.APP)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndChannelAndIsReadFalse(userId, NotificationChannel.APP);
    }

    @Override
    @Transactional
    public void markAsRead(UUID notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByRecipientId(userId);
    }

    @Override
    @Transactional
    public void deleteNotification(UUID notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    // ────────────────────────────────────────────────────────────
    // Multi-Channel Dispatch
    // ────────────────────────────────────────────────────────────

    /**
     * Creates an in-app notification and dispatches to external channels (Email, SMS).
     * Each channel operates independently — a failure in one doesn't block others.
     */
    @Override
    public void createAndSendNotification(Long recipientId, NotificationType type,
                                           String title, String message,
                                           Long relatedId, RelatedType relatedType) {
        // 1. Save In-App notification
        Notification notification = Notification.builder()
                .recipientId(recipientId)
                .type(type)
                .title(title)
                .message(message)
                .channel(NotificationChannel.APP)
                .relatedId(relatedId)
                .relatedType(relatedType)
                .deliveryStatus(ReminderStatus.SENT)
                .build();
        notificationRepository.save(notification);
        log.debug("In-app notification saved for user {}: {}", recipientId, title);

        // 2. Dispatch to external channels (async, fault-tolerant)
        Optional<User> userOpt = userRepository.findById(recipientId);
        if (userOpt.isEmpty()) {
            log.warn("Recipient user {} not found — skipping external channels", recipientId);
            return;
        }

        User user = userOpt.get();
        dispatchEmail(user, title, message, relatedId, relatedType, type);
        dispatchSms(user, message, relatedId, relatedType, type);
    }

    /**
     * Retry a previously failed notification delivery.
     * Called by ReminderProcessorScheduler's retry job.
     */
    @Override
    public void retryNotification(Notification notification) {
        Optional<User> userOpt = userRepository.findById(notification.getRecipientId());
        if (userOpt.isEmpty()) {
            log.warn("Cannot retry notification {} — recipient {} not found",
                    notification.getNotificationId(), notification.getRecipientId());
            return;
        }

        User user = userOpt.get();

        if (notification.getChannel() == NotificationChannel.EMAIL) {
            emailService.sendEmail(user.getEmail(), notification.getTitle(), notification.getMessage());
        } else if (notification.getChannel() == NotificationChannel.SMS) {
            smsService.sendSms(user.getPhone(), notification.getMessage()); // Now using phone field
        }

        log.info("Retried notification {} via {} for user {}",
                notification.getNotificationId(), notification.getChannel(), user.getId());
    }

    // ────────────────────────────────────────────────────────────
    // Private Helpers
    // ────────────────────────────────────────────────────────────

    /**
     * Dispatch email notification asynchronously.
     * Failures are logged and tracked but don't break the flow.
     */
    private void dispatchEmail(User user, String title, String message,
                                Long relatedId, RelatedType relatedType, NotificationType type) {
        try {
            emailService.sendEmail(user.getEmail(), title, message);

            // Track email dispatch
            Notification emailNotification = Notification.builder()
                    .recipientId(user.getId())
                    .type(type)
                    .title(title)
                    .message(message)
                    .channel(NotificationChannel.EMAIL)
                    .relatedId(relatedId)
                    .relatedType(relatedType)
                    .deliveryStatus(ReminderStatus.SENT)
                    .build();
            notificationRepository.save(emailNotification);

        } catch (Exception e) {
            log.error("Email dispatch failed for user {}: {}", user.getId(), e.getMessage());

            // Save as FAILED for retry
            Notification failedEmail = Notification.builder()
                    .recipientId(user.getId())
                    .type(type)
                    .title(title)
                    .message(message)
                    .channel(NotificationChannel.EMAIL)
                    .relatedId(relatedId)
                    .relatedType(relatedType)
                    .deliveryStatus(ReminderStatus.FAILED)
                    .build();
            notificationRepository.save(failedEmail);
        }
    }

    /**
     * Dispatch SMS notification asynchronously.
     * Uses stub implementation — replace with Twilio/AWS SNS for production.
     */
    private void dispatchSms(User user, String message,
                              Long relatedId, RelatedType relatedType, NotificationType type) {
        try {
            smsService.sendSms(user.getPhone(), message);

            Notification smsNotification = Notification.builder()
                    .recipientId(user.getId())
                    .type(type)
                    .title("SMS Notification")
                    .message(message)
                    .channel(NotificationChannel.SMS)
                    .relatedId(relatedId)
                    .relatedType(relatedType)
                    .deliveryStatus(ReminderStatus.SENT)
                    .build();
            notificationRepository.save(smsNotification);

        } catch (Exception e) {
            log.error("SMS dispatch failed for user {}: {}", user.getId(), e.getMessage());

            Notification failedSms = Notification.builder()
                    .recipientId(user.getId())
                    .type(type)
                    .title("SMS Notification")
                    .message(message)
                    .channel(NotificationChannel.SMS)
                    .relatedId(relatedId)
                    .relatedType(relatedType)
                    .deliveryStatus(ReminderStatus.FAILED)
                    .build();
            notificationRepository.save(failedSms);
        }
    }

    private NotificationDTO mapToDTO(Notification notification) {
        return NotificationDTO.builder()
                .notificationId(notification.getNotificationId())
                .recipientId(notification.getRecipientId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .channel(notification.getChannel())
                .relatedId(notification.getRelatedId())
                .relatedType(notification.getRelatedType())
                .isRead(notification.isRead())
                .sentAt(notification.getSentAt())
                .build();
    }
}
