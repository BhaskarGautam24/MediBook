package com.medibook.review.service;

import com.medibook.review.client.*;
import com.medibook.review.dto.*;
import com.medibook.review.entity.Review;
import com.medibook.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final AuthClient authClient;
    private final ProviderClient providerClient;
    private final AppointmentClient appointmentClient;
    private final NotificationClient notificationClient;

    @Transactional
    public ReviewResponse addReview(Long patientUserId, ReviewRequest request) {
        // Validate appointment via Feign
        Map<String, Object> appointment = appointmentClient.getAppointmentById(request.getAppointmentId());
        if (appointment == null) throw new RuntimeException("Appointment not found");
        if (!"COMPLETED".equals(appointment.get("status"))) throw new RuntimeException("You can only review completed appointments");
        if (!patientUserId.equals(Long.valueOf(appointment.get("patientId").toString()))) throw new RuntimeException("You can only review your own appointments");
        if (reviewRepository.existsByAppointmentId(request.getAppointmentId())) throw new RuntimeException("You have already reviewed this appointment");

        Long providerId = Long.valueOf(appointment.get("providerId").toString());

        Review review = Review.builder()
                .appointmentId(request.getAppointmentId())
                .patientId(patientUserId)
                .providerId(providerId)
                .rating(request.getRating())
                .comment(request.getComment())
                .isAnonymous(request.getIsAnonymous() != null ? request.getIsAnonymous() : false)
                .isVerified(true).isFlagged(false).build();

        review = reviewRepository.save(review);
        recalculateProviderRating(providerId);

        // Notify provider
        try {
            Map<String, Object> provider = providerClient.getProviderById(providerId);
            Long providerUserId = Long.valueOf(provider.get("userId").toString());
            String patientName = review.getIsAnonymous() ? "A patient" : "Patient";
            try {
                Map<String, Object> patient = authClient.getUserById(patientUserId);
                if (!review.getIsAnonymous()) patientName = (String) patient.getOrDefault("name", "Patient");
            } catch (Exception e) { /* fallback */ }

            notificationClient.sendNotification(Map.of(
                    "recipientId", providerUserId, "type", "REVIEW_RECEIVED", "title", "New Review Received",
                    "message", String.format("%s rated you %d/5 star%s. \"%s\"", patientName, review.getRating(),
                            review.getRating() == 1 ? "" : "s", review.getComment() != null ? review.getComment() : "No comment"),
                    "relatedId", review.getId(), "relatedType", "REVIEW"
            ));
        } catch (Exception e) { log.warn("Failed to send review notification: {}", e.getMessage()); }

        return toResponse(review);
    }

    @Transactional
    public ReviewResponse updateReview(Long reviewId, Long patientUserId, ReviewRequest request) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new RuntimeException("Review not found"));
        if (!review.getPatientId().equals(patientUserId)) throw new RuntimeException("You can only edit your own reviews");
        review.setRating(request.getRating());
        if (request.getComment() != null) review.setComment(request.getComment());
        if (request.getIsAnonymous() != null) review.setIsAnonymous(request.getIsAnonymous());
        review = reviewRepository.save(review);
        recalculateProviderRating(review.getProviderId());
        return toResponse(review);
    }

    @Transactional
    public void deleteReview(Long reviewId, Long userId, String userRole) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new RuntimeException("Review not found"));
        if (!"ROLE_ADMIN".equals(userRole) && !review.getPatientId().equals(userId))
            throw new RuntimeException("You can only delete your own reviews");
        Long providerId = review.getProviderId();
        reviewRepository.delete(review);
        recalculateProviderRating(providerId);
    }

    public ReviewResponse getReviewById(Long reviewId) {
        return toResponse(reviewRepository.findById(reviewId).orElseThrow(() -> new RuntimeException("Review not found")));
    }

    public Page<ReviewResponse> getReviewsByProvider(Long providerId, Pageable pageable) {
        return reviewRepository.findByProviderId(providerId, pageable).map(this::toResponse);
    }

    public Page<ReviewResponse> getReviewsByPatient(Long patientUserId, Pageable pageable) {
        return reviewRepository.findByPatientId(patientUserId, pageable).map(this::toResponse);
    }

    public Map<String, Object> getProviderRatingSummary(Long providerId) {
        Double avg = reviewRepository.avgRatingByProviderId(providerId);
        long total = reviewRepository.countActiveByProviderId(providerId);
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("providerId", providerId);
        summary.put("averageRating", avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0);
        summary.put("totalReviews", total);
        return summary;
    }

    public boolean hasReviewForAppointment(Long appointmentId) {
        return reviewRepository.existsByAppointmentId(appointmentId);
    }

    @Transactional
    public void flagReview(Long reviewId, Long providerUserId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new RuntimeException("Review not found"));
        Map<String, Object> provider = providerClient.getProviderByUserId(providerUserId);
        Long providerId = Long.valueOf(provider.get("id").toString());
        if (!review.getProviderId().equals(providerId)) throw new RuntimeException("You can only flag reviews on your own profile");
        review.setIsFlagged(true);
        reviewRepository.save(review);
    }

    @Transactional public void unflagReview(Long reviewId) {
        Review r = reviewRepository.findById(reviewId).orElseThrow(() -> new RuntimeException("Review not found"));
        r.setIsFlagged(false); reviewRepository.save(r);
    }

    @Transactional public void moderateDeleteReview(Long reviewId) {
        Review r = reviewRepository.findById(reviewId).orElseThrow(() -> new RuntimeException("Review not found"));
        Long pid = r.getProviderId(); reviewRepository.delete(r); recalculateProviderRating(pid);
    }

    public Page<ReviewResponse> getFlaggedReviews(Pageable pageable) {
        return reviewRepository.findByIsFlaggedTrue(pageable).map(this::toResponse);
    }

    private void recalculateProviderRating(Long providerId) {
        Double avg = reviewRepository.avgRatingByProviderId(providerId);
        long total = reviewRepository.countActiveByProviderId(providerId);
        try {
            providerClient.updateRating(providerId, Map.of(
                    "averageRating", avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0,
                    "totalReviews", (int) total));
        } catch (Exception e) { log.warn("Failed to update provider rating: {}", e.getMessage()); }
    }

    private ReviewResponse toResponse(Review review) {
        String patientName = "Anonymous";
        if (!Boolean.TRUE.equals(review.getIsAnonymous())) {
            try { Map<String, Object> p = authClient.getUserById(review.getPatientId()); patientName = (String) p.getOrDefault("name", "Patient"); }
            catch (Exception e) { patientName = "Patient"; }
        }
        String providerName = "Doctor";
        try { Map<String, Object> p = providerClient.getProviderById(review.getProviderId()); providerName = (String) p.getOrDefault("userName", "Doctor"); }
        catch (Exception e) { /* fallback */ }

        return ReviewResponse.builder().id(review.getId()).appointmentId(review.getAppointmentId())
                .patientId(review.getPatientId()).providerId(review.getProviderId())
                .patientName(patientName).providerName(providerName)
                .rating(review.getRating()).comment(review.getComment())
                .isAnonymous(review.getIsAnonymous()).isVerified(review.getIsVerified()).isFlagged(review.getIsFlagged())
                .createdAt(review.getCreatedAt()).updatedAt(review.getUpdatedAt()).build();
    }
}
