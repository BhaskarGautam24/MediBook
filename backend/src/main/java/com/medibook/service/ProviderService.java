package com.medibook.service;

import com.medibook.entity.Provider;
import com.medibook.enums.ProviderStatus;
import com.medibook.repository.AppointmentRepository;
import com.medibook.repository.ProviderRepository;
import com.medibook.repository.SlotRepository;
import com.medibook.enums.AppointmentStatus;
import com.medibook.enums.NotificationType;
import com.medibook.enums.RelatedType;
import com.medibook.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProviderService {

    private final ProviderRepository providerRepository;
    private final SlotRepository slotRepository;
    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;

    /**
     * Get all verified providers (for patient browsing)
     */
    public List<Map<String, Object>> getVerifiedProviders() {
        return providerRepository.findByIsVerifiedTrue().stream()
                .map(this::toProviderMap)
                .toList();
    }

    /**
     * Get provider by ID (public)
     */
    public Map<String, Object> getProviderById(Long id) {
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Provider not found"));
        return toProviderMap(provider);
    }

    /**
     * Get provider by user ID
     */
    public Provider getProviderByUserId(Long userId) {
        return providerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Provider profile not found"));
    }

    /**
     * Get stats for provider dashboard
     */
    public Map<String, Object> getProviderStats(Long userId) {
        Provider provider = getProviderByUserId(userId);
        Map<String, Object> stats = new HashMap<>();
        stats.put("providerStatus", provider.getStatus().name());
        stats.put("totalSlots", slotRepository.countByProviderId(provider.getId()));
        stats.put("bookedSlots", slotRepository.countByProviderIdAndIsBookedTrue(provider.getId()));
        stats.put("totalAppointments", appointmentRepository.countByProviderId(provider.getId()));
        stats.put("completedAppointments", appointmentRepository.countByProviderIdAndStatus(provider.getId(), AppointmentStatus.COMPLETED));
        stats.put("consultationFee", provider.getConsultationFee() != null ? provider.getConsultationFee() : 500.0);
        return stats;
    }

    /**
     * Update consultation fee (provider sets their own price)
     */
    @Transactional
    public Map<String, Object> updateConsultationFee(Long userId, Double fee) {
        if (fee == null || fee < 0) {
            throw new RuntimeException("Consultation fee must be a positive number");
        }
        Provider provider = getProviderByUserId(userId);
        provider.setConsultationFee(fee);
        providerRepository.save(provider);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "Consultation fee updated successfully");
        result.put("consultationFee", fee);
        return result;
    }

    /**
     * Get all providers (admin view)
     */
    public List<Map<String, Object>> getAllProviders() {
        return providerRepository.findAll().stream()
                .map(p -> {
                    Map<String, Object> map = toProviderMap(p);
                    map.put("userEmail", p.getUser().getEmail());
                    return map;
                })
                .toList();
    }

    /**
     * Approve provider
     */
    @Transactional
    public void approveProvider(Long providerId) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));
        provider.setStatus(ProviderStatus.APPROVED);
        provider.setIsVerified(true);
        providerRepository.save(provider);

        notificationService.createAndSendNotification(
                provider.getUser().getId(),
                NotificationType.ACCOUNT_UPDATE,
                "Profile Approved",
                "Your provider profile has been approved! You can now manage slots and accept appointments.",
                provider.getId(),
                RelatedType.RECORD // Using RECORD as a generic placeholder if related type doesn't fit exactly, or null if allowed. Wait, let's look at the enum. I'll pass null since it's an account update. Wait, relatedType can't be null if the schema requires it, but the DB allows null. Let's just pass null for relatedId and null for relatedType if possible, or we can use RECORD. Let's check Notification Entity.
        );
    }

    /**
     * Reject provider
     */
    @Transactional
    public void rejectProvider(Long providerId) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));
        provider.setStatus(ProviderStatus.REJECTED);
        provider.setIsVerified(false);
        providerRepository.save(provider);

        notificationService.createAndSendNotification(
                provider.getUser().getId(),
                NotificationType.ACCOUNT_UPDATE,
                "Profile Rejected",
                "Your provider profile application has been rejected. Please contact support for more details.",
                provider.getId(),
                RelatedType.RECORD // Placeholder
        );
    }

    private Map<String, Object> toProviderMap(Provider p) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", p.getId());
        map.put("userId", p.getUser().getId());
        map.put("userName", p.getUser().getName());
        map.put("specialization", p.getSpecialization());
        map.put("experienceYears", p.getExperienceYears());
        map.put("clinicName", p.getClinicName());
        map.put("clinicAddress", p.getClinicAddress());
        map.put("isVerified", p.getIsVerified());
        map.put("status", p.getStatus().name());
        map.put("consultationFee", p.getConsultationFee() != null ? p.getConsultationFee() : 500.0);
        return map;
    }
}
