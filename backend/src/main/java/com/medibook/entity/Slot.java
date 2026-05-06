package com.medibook.entity;

import com.medibook.enums.RecurrenceType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "slots", indexes = {
    @Index(name = "idx_slot_provider_date", columnList = "provider_id, date"),
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_slot_provider_date_time",
            columnNames = {"provider_id", "date", "start_time", "end_time"})
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Slot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "is_booked")
    @Builder.Default
    private Boolean isBooked = false;

    @Column(name = "is_blocked")
    @Builder.Default
    private Boolean isBlocked = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_type", length = 10)
    @Builder.Default
    private RecurrenceType recurrenceType = RecurrenceType.NONE;

    @Version
    private Long version;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.durationMinutes == null && this.startTime != null && this.endTime != null) {
            this.durationMinutes = (int) java.time.Duration.between(this.startTime, this.endTime).toMinutes();
        }
    }
}
