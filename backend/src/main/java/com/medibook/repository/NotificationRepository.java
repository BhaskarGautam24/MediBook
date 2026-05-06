package com.medibook.repository;

import com.medibook.entity.Notification;
import com.medibook.enums.NotificationChannel;
import com.medibook.enums.ReminderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    
    List<Notification> findByRecipientIdAndChannelOrderBySentAtDesc(Long recipientId, NotificationChannel channel);
    
    long countByRecipientIdAndChannelAndIsReadFalse(Long recipientId, NotificationChannel channel);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipientId = :recipientId AND n.channel = 'APP' AND n.isRead = false")
    void markAllAsReadByRecipientId(Long recipientId);

    /** Find failed notifications eligible for retry (retryCount < maxRetries) */
    List<Notification> findByDeliveryStatusAndRetryCountLessThan(ReminderStatus status, int maxRetries);
}

