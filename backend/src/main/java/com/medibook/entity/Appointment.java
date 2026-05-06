package com.medibook.entity;

import com.medibook.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "appointments")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "slot_id", nullable = true)
    private Slot slot;

    // ── Snapshot fields: preserve slot info after slot deletion ──
    @Column(name = "slot_date")
    private LocalDate slotDate;

    @Column(name = "slot_start_time")
    private LocalTime slotStartTime;

    @Column(name = "slot_end_time")
    private LocalTime slotEndTime;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.PENDING;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        snapshotSlotData();
    }

    @PreUpdate
    protected void onUpdate() {
        snapshotSlotData();
    }

    /** Copy slot date/time to snapshot fields so data survives slot deletion */
    private void snapshotSlotData() {
        if (this.slot != null) {
            this.slotDate = this.slot.getDate();
            this.slotStartTime = this.slot.getStartTime();
            this.slotEndTime = this.slot.getEndTime();
        }
    }
}
