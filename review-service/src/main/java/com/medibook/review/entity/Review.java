package com.medibook.review.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews", indexes = {
        @Index(name = "idx_review_provider", columnList = "provider_id"),
        @Index(name = "idx_review_patient", columnList = "patient_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Review {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "appointment_id", nullable = false, unique = true) private Long appointmentId;
    @Column(name = "patient_id", nullable = false) private Long patientId;
    @Column(name = "provider_id", nullable = false) private Long providerId;
    @Min(1) @Max(5) @Column(nullable = false) private Integer rating;
    @Size(max = 1000) @Column(length = 1000) private String comment;
    @Column(name = "is_anonymous") @Builder.Default private Boolean isAnonymous = false;
    @Column(name = "is_verified") @Builder.Default private Boolean isVerified = true;
    @Column(name = "is_flagged") @Builder.Default private Boolean isFlagged = false;
    @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at") private LocalDateTime updatedAt;
    @PrePersist protected void onCreate() { this.createdAt = LocalDateTime.now(); this.updatedAt = this.createdAt; }
    @PreUpdate protected void onUpdate() { this.updatedAt = LocalDateTime.now(); }
}
