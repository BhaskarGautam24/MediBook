package com.medibook.provider.service;

import com.medibook.provider.client.AuthClient;
import com.medibook.provider.client.NotificationClient;
import com.medibook.provider.dto.ProviderProfileUpdateRequest;
import com.medibook.provider.entity.Provider;
import com.medibook.provider.enums.ProviderStatus;
import com.medibook.provider.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProviderService {

    private final ProviderRepository providerRepository;
    private final AuthClient authClient;
    private final NotificationClient notificationClient;

    public List<Map<String, Object>> getVerifiedProviders() {
        return providerRepository.findByIsVerifiedTrue().stream()
                .map(this::toProviderMap)
                .toList();
    }

    public Map<String, Object> getProviderById(Long id) {
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Provider not found"));
        return toProviderMap(provider);
    }

    public Provider getProviderByUserId(Long userId) {
        return providerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Provider profile not found"));
    }

    /**
     * Dashboard stats. Slot/appointment counts fetched via Feign or computed locally.
     * For now, simplified — only provider-owned data.
     * Slot/appointment counts will be added when those services are ready.
     */
    public Map<String, Object> getProviderStats(Long userId) {
        Provider provider = getProviderByUserId(userId);
        Map<String, Object> stats = new HashMap<>();
        stats.put("providerStatus", provider.getStatus().name());
        stats.put("consultationFee", provider.getConsultationFee() != null ? provider.getConsultationFee() : 500.0);
        stats.put("averageRating", provider.getAverageRating() != null ? provider.getAverageRating() : 0.0);
        stats.put("totalReviews", provider.getTotalReviews() != null ? provider.getTotalReviews() : 0);
        // Slot/appointment counts will be populated by schedule-service and appointment-service
        stats.put("totalSlots", 0);
        stats.put("bookedSlots", 0);
        stats.put("totalAppointments", 0);
        stats.put("completedAppointments", 0);
        return stats;
    }

    public Map<String, Object> getMyProfile(Long userId) {
        Provider provider = getProviderByUserId(userId);
        Map<String, Object> map = toProviderMap(provider);
        map.put("qualifications", provider.getQualifications() != null ? provider.getQualifications() : "");
        map.put("bio", provider.getBio() != null ? provider.getBio() : "");
        map.put("profilePicture", provider.getProfilePicture() != null ? provider.getProfilePicture() : "");

        // Get email/phone from auth-service
        try {
            Map<String, Object> user = authClient.getUserById(provider.getUserId());
            map.put("email", user.getOrDefault("email", ""));
            map.put("phone", user.getOrDefault("phone", ""));
        } catch (Exception e) {
            log.warn("Failed to fetch user details from auth-service: {}", e.getMessage());
            map.put("email", "");
            map.put("phone", "");
        }

        return map;
    }

    @Transactional
    public Map<String, Object> updateMyProfile(Long userId, ProviderProfileUpdateRequest request) {
        Provider provider = getProviderByUserId(userId);

        if (request.getSpecialization() != null) provider.setSpecialization(request.getSpecialization());
        if (request.getQualifications() != null) provider.setQualifications(request.getQualifications());
        if (request.getBio() != null) provider.setBio(request.getBio());
        if (request.getExperienceYears() != null) provider.setExperienceYears(request.getExperienceYears());
        if (request.getClinicName() != null) provider.setClinicName(request.getClinicName());
        if (request.getClinicAddress() != null) provider.setClinicAddress(request.getClinicAddress());
        if (request.getConsultationFee() != null) provider.setConsultationFee(request.getConsultationFee());

        providerRepository.save(provider);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "Profile updated successfully");
        result.putAll(getMyProfile(userId));
        return result;
    }

    @Transactional
    public String updateProfilePicture(Long userId, String imageUrl) {
        Provider provider = getProviderByUserId(userId);
        String oldPicture = provider.getProfilePicture();
        provider.setProfilePicture(imageUrl);
        providerRepository.save(provider);
        return oldPicture;
    }

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

    public List<Map<String, Object>> getAllProviders() {
        return providerRepository.findAll().stream()
                .map(p -> {
                    Map<String, Object> map = toProviderMap(p);
                    try {
                        Map<String, Object> user = authClient.getUserById(p.getUserId());
                        map.put("userEmail", user.getOrDefault("email", ""));
                    } catch (Exception e) {
                        map.put("userEmail", "");
                    }
                    return map;
                })
                .toList();
    }

    public List<Map<String, Object>> getPendingProviders() {
        return providerRepository.findByStatus(ProviderStatus.PENDING).stream()
                .map(p -> {
                    Map<String, Object> map = toProviderMap(p);
                    try {
                        Map<String, Object> user = authClient.getUserById(p.getUserId());
                        map.put("userEmail", user.getOrDefault("email", ""));
                    } catch (Exception e) {
                        map.put("userEmail", "");
                    }
                    return map;
                })
                .toList();
    }

    @Transactional
    public void approveProvider(Long providerId) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));
        provider.setStatus(ProviderStatus.APPROVED);
        provider.setIsVerified(true);
        providerRepository.save(provider);

        try {
            notificationClient.sendNotification(Map.of(
                    "recipientId", provider.getUserId(),
                    "type", "ACCOUNT_UPDATE",
                    "title", "Profile Approved",
                    "message", "Your provider profile has been approved! You can now manage slots and accept appointments.",
                    "relatedId", provider.getId(),
                    "relatedType", "RECORD"
            ));
        } catch (Exception e) {
            log.warn("Failed to send approval notification: {}", e.getMessage());
        }
    }

    @Transactional
    public void rejectProvider(Long providerId) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));
        provider.setStatus(ProviderStatus.REJECTED);
        provider.setIsVerified(false);
        providerRepository.save(provider);

        try {
            notificationClient.sendNotification(Map.of(
                    "recipientId", provider.getUserId(),
                    "type", "ACCOUNT_UPDATE",
                    "title", "Profile Rejected",
                    "message", "Your provider profile application has been rejected. Please contact support.",
                    "relatedId", provider.getId(),
                    "relatedType", "RECORD"
            ));
        } catch (Exception e) {
            log.warn("Failed to send rejection notification: {}", e.getMessage());
        }
    }

    /** Update rating — called by review-service via internal API */
    @Transactional
    public void updateRating(Long providerId, Double averageRating, Integer totalReviews) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));
        provider.setAverageRating(averageRating);
        provider.setTotalReviews(totalReviews);
        providerRepository.save(provider);
    }

    private Map<String, Object> toProviderMap(Provider p) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", p.getId());
        map.put("userId", p.getUserId());

        // Fetch user name from auth-service
        String userName = "Unknown";
        try {
            Map<String, Object> user = authClient.getUserById(p.getUserId());
            userName = (String) user.getOrDefault("name", "Unknown");
        } catch (Exception e) {
            log.debug("Could not fetch user name for provider {}: {}", p.getId(), e.getMessage());
        }

        map.put("userName", userName);
        map.put("specialization", p.getSpecialization());
        map.put("experienceYears", p.getExperienceYears());
        map.put("clinicName", p.getClinicName());
        map.put("clinicAddress", p.getClinicAddress());
        map.put("isVerified", p.getIsVerified());
        map.put("status", p.getStatus().name());
        map.put("consultationFee", p.getConsultationFee() != null ? p.getConsultationFee() : 500.0);
        map.put("averageRating", p.getAverageRating() != null ? p.getAverageRating() : 0.0);
        map.put("totalReviews", p.getTotalReviews() != null ? p.getTotalReviews() : 0);
        map.put("qualifications", p.getQualifications() != null ? p.getQualifications() : "");
        map.put("bio", p.getBio() != null ? p.getBio() : "");
        map.put("profilePicture", p.getProfilePicture() != null ? p.getProfilePicture() : "");
        return map;
    }
}
