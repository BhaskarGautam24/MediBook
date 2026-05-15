package com.medibook.record.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name = "medical_records")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MedicalRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id") private Long recordId;
    @Column(name = "appointment_id", nullable = false, unique = true) private Long appointmentId;
    @Column(name = "patient_id", nullable = false) private Long patientId;
    @Column(name = "provider_id", nullable = false) private Long providerId;
    @Column(nullable = false, columnDefinition = "TEXT") private String diagnosis;
    @Column(nullable = false, columnDefinition = "TEXT") private String prescription;
    @Column(columnDefinition = "TEXT") private String notes;
    @Column(name = "follow_up_date") private LocalDate followUpDate;
    @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at") private LocalDateTime updatedAt;
    @PrePersist protected void onCreate() { this.createdAt = LocalDateTime.now(); this.updatedAt = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { this.updatedAt = LocalDateTime.now(); }
}
