package com.medibook.notification.service;

import com.medibook.notification.dto.NotificationDTO;
import com.medibook.notification.entity.Notification;
import com.medibook.notification.enums.*;
import com.medibook.notification.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<NotificationDTO> getUserNotifications(Long userId) {
        return notificationRepository.findByRecipientIdAndChannelOrderBySentAtDesc(userId, NotificationChannel.APP)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndChannelAndIsReadFalse(userId, NotificationChannel.APP);
    }

    @Transactional
    public void markAsRead(UUID notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> { n.setRead(true); notificationRepository.save(n); });
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByRecipientId(userId);
    }

    @Transactional
    public void deleteNotification(UUID notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    /**
     * Create and save in-app notification.
     * Called by other services via internal API.
     */
    public void createAndSendNotification(Long recipientId, String type, String title, String message, Long relatedId, String relatedType) {
        Notification notification = Notification.builder()
                .recipientId(recipientId)
                .type(NotificationType.valueOf(type))
                .title(title)
                .message(message)
                .channel(NotificationChannel.APP)
                .relatedId(relatedId)
                .relatedType(relatedType != null ? RelatedType.valueOf(relatedType) : null)
                .isRead(false)
                .deliveryStatus(ReminderStatus.SENT)
                .build();
        notificationRepository.save(notification);
        log.info("Notification sent: type={} recipient={} title={}", type, recipientId, title);
    }

    private NotificationDTO mapToDTO(Notification n) {
        return NotificationDTO.builder()
                .notificationId(n.getNotificationId())
                .recipientId(n.getRecipientId())
                .type(n.getType().name())
                .title(n.getTitle())
                .message(n.getMessage())
                .channel(n.getChannel().name())
                .relatedId(n.getRelatedId())
                .relatedType(n.getRelatedType() != null ? n.getRelatedType().name() : null)
                .isRead(n.isRead())
                .sentAt(n.getSentAt())
                .build();
    }
}
