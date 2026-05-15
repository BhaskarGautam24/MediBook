package com.medibook.notification.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Notification message payload")
public class NotificationDTO {
    @Schema(description = "Unique ID of the notification", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID notificationId;
    @Schema(description = "User ID of the recipient", example = "10")
    private Long recipientId;
    @Schema(description = "Type of notification", example = "APPOINTMENT_REMINDER")
    private String type;
    @Schema(description = "Notification title", example = "Appointment Tomorrow")
    private String title;
    @Schema(description = "Notification body", example = "Your appointment is scheduled for tomorrow at 10:00 AM")
    private String message;
    @Schema(description = "Delivery channel", example = "IN_APP")
    private String channel;
    @Schema(description = "ID of the related entity (e.g. appointmentId)", example = "42")
    private Long relatedId;
    @Schema(description = "Type of the related entity", example = "APPOINTMENT")
    private String relatedType;
    @Schema(description = "Whether the notification has been read", example = "false")
    private boolean isRead;
    @Schema(description = "Timestamp when sent")
    private LocalDateTime sentAt;
}
