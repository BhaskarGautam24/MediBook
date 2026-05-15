package com.medibook.provider.entity;

import com.medibook.provider.enums.ProviderStatus;
import jakarta.persistence.*;
import lombok.*;

/**
 * Provider entity — stores healthcare provider (doctor) data.
 * In microservices, userId is a Long reference (no JPA FK to User).
 * User details are fetched via Feign client to auth-service.
 */
@Entity
@Table(name = "providers")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Provider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Reference to User in auth-service (no JPA FK) */
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(length = 100)
    private String specialization;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "clinic_name", length = 200)
    private String clinicName;

    @Column(name = "clinic_address", length = 500)
    private String clinicAddress;

    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private ProviderStatus status = ProviderStatus.PENDING;

    @Column(name = "consultation_fee")
    @Builder.Default
    private Double consultationFee = 500.0;

    @Column(name = "average_rating")
    @Builder.Default
    private Double averageRating = 0.0;

    @Column(name = "total_reviews")
    @Builder.Default
    private Integer totalReviews = 0;

    @Column(length = 500)
    private String qualifications;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "profile_picture", length = 500)
    private String profilePicture;
}
