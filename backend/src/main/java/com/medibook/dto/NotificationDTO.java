package com.medibook.dto;

import com.medibook.enums.NotificationChannel;
import com.medibook.enums.NotificationType;
import com.medibook.enums.RelatedType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private UUID notificationId;
    private Long recipientId;
    private NotificationType type;
    private String title;
    private String message;
    private NotificationChannel channel;
    private Long relatedId;
    private RelatedType relatedType;
    private boolean isRead;
    private LocalDateTime sentAt;
}
