package com.medibook.notification.entity;

import com.medibook.notification.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "notifications")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID notificationId;
    @Column(nullable = false) private Long recipientId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40) private NotificationType type;
    @Column(nullable = false) private String title;
    @Column(columnDefinition = "TEXT", nullable = false) private String message;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private NotificationChannel channel;
    private Long relatedId;
    @Enumerated(EnumType.STRING) @Column(length = 20) private RelatedType relatedType;
    @Column(nullable = false) @Builder.Default private boolean isRead = false;
    @Enumerated(EnumType.STRING) @Column(length = 20) @Builder.Default private ReminderStatus deliveryStatus = ReminderStatus.SENT;
    @Column(nullable = false) @Builder.Default private int retryCount = 0;
    @CreationTimestamp @Column(nullable = false, updatable = false) private LocalDateTime sentAt;
}
