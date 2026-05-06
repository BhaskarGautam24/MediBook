package com.medibook.entity;

import com.medibook.enums.ProviderStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "providers")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Provider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

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

    // Consultation fee in INR (default ₹500)
    @Column(name = "consultation_fee")
    @Builder.Default
    private Double consultationFee = 500.0;
}
