package com.medibook.notification.repository;

import com.medibook.notification.entity.Notification;
import com.medibook.notification.enums.NotificationChannel;
import com.medibook.notification.enums.ReminderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByRecipientIdAndChannelOrderBySentAtDesc(Long recipientId, NotificationChannel channel);
    long countByRecipientIdAndChannelAndIsReadFalse(Long recipientId, NotificationChannel channel);
    @Modifying @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipientId = :recipientId AND n.channel = 'APP' AND n.isRead = false")
    void markAllAsReadByRecipientId(Long recipientId);
    List<Notification> findByDeliveryStatusAndRetryCountLessThan(ReminderStatus status, int maxRetries);
}
