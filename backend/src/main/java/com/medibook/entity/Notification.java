package com.medibook.entity;

import com.medibook.enums.NotificationChannel;
import com.medibook.enums.NotificationType;
import com.medibook.enums.RelatedType;
import com.medibook.enums.ReminderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID notificationId;

    @Column(nullable = false)
    private Long recipientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;

    private Long relatedId;

    @Enumerated(EnumType.STRING)
    private RelatedType relatedType;

    @Column(nullable = false)
    @Builder.Default
    private boolean isRead = false;

    /** Delivery status for retry tracking */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private ReminderStatus deliveryStatus = ReminderStatus.SENT;

    /** Number of delivery attempts (for email/SMS retry) */
    @Column(nullable = false)
    @Builder.Default
    private int retryCount = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime sentAt;
}

