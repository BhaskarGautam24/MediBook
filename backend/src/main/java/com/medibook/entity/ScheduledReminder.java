package com.medibook.entity;

import com.medibook.enums.NotificationType;
import com.medibook.enums.ReminderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a pre-scheduled reminder for an appointment.
 * Created when appointment reaches SCHEDULED status.
 * Processed by ReminderProcessorScheduler when scheduledAt <= now.
 */
@Entity
@Table(name = "scheduled_reminders", uniqueConstraints = {
    @UniqueConstraint(name = "uk_idempotency_key", columnNames = {"idempotency_key"})
}, indexes = {
    @Index(name = "idx_status_scheduled_at", columnList = "status, scheduled_at"),
    @Index(name = "idx_appointment_id", columnList = "appointment_id")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ScheduledReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "appointment_id", nullable = false)
    private Long appointmentId;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reminder_type", nullable = false, length = 30)
    private NotificationType reminderType;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReminderStatus status = ReminderStatus.PENDING;

    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String idempotencyKey;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
