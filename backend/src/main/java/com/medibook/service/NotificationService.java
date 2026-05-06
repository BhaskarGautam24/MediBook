package com.medibook.service;

import com.medibook.dto.NotificationDTO;
import com.medibook.entity.Notification;
import com.medibook.enums.NotificationType;
import com.medibook.enums.RelatedType;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    List<NotificationDTO> getUserNotifications(Long userId);

    long getUnreadCount(Long userId);

    void markAsRead(UUID notificationId);

    void markAllAsRead(Long userId);

    void deleteNotification(UUID notificationId);

    // Internal methods for generating specific notifications
    void createAndSendNotification(Long recipientId, NotificationType type, String title, String message, Long relatedId, RelatedType relatedType);

    /** Retry a previously failed notification (email/SMS) */
    void retryNotification(Notification notification);
}

