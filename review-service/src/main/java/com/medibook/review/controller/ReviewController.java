package com.medibook.review.controller;

import com.medibook.review.dto.*;
import com.medibook.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Endpoints for patient reviews and provider ratings")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Add a review", description = "Submits a review for a completed appointment")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ReviewResponse> addReview(@RequestHeader("X-User-Id") Long userId, @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.addReview(userId, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get review by ID", description = "Retrieves a specific review")
    public ResponseEntity<ReviewResponse> getReview(@PathVariable Long id) { return ResponseEntity.ok(reviewService.getReviewById(id)); }

    @PutMapping("/{id}")
    @Operation(summary = "Update review", description = "Updates an existing review by the original author")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ReviewResponse> updateReview(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId, @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(reviewService.updateReview(id, userId, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete review", description = "Deletes a review by the original author or admin")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId, @RequestHeader(value = "X-User-Role", defaultValue = "") String role) {
        reviewService.deleteReview(id, userId, role);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/provider/{providerId}")
    @Operation(summary = "Get provider reviews", description = "Retrieves paginated reviews for a specific provider")
    public ResponseEntity<Page<ReviewResponse>> getProviderReviews(@PathVariable Long providerId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.getReviewsByProvider(providerId, PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @GetMapping("/provider/{providerId}/summary")
    @Operation(summary = "Get provider rating summary", description = "Retrieves average rating and total review count for a provider")
    public ResponseEntity<Map<String, Object>> getProviderRatingSummary(@PathVariable Long providerId) {
        return ResponseEntity.ok(reviewService.getProviderRatingSummary(providerId));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my reviews", description = "Retrieves all reviews written by the authenticated patient")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Page<ReviewResponse>> getMyReviews(@RequestHeader("X-User-Id") Long userId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.getReviewsByPatient(userId, PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @GetMapping("/check/{appointmentId}")
    @Operation(summary = "Check if reviewed", description = "Checks whether a review exists for a specific appointment")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, Object>> checkReview(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(Map.of("reviewed", reviewService.hasReviewForAppointment(appointmentId)));
    }

    @PutMapping("/{id}/flag")
    @Operation(summary = "Flag a review", description = "Flags a review for admin moderation")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, String>> flagReview(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        reviewService.flagReview(id, userId);
        return ResponseEntity.ok(Map.of("message", "Review flagged for moderation"));
    }

    @GetMapping("/admin/flagged")
    @Operation(summary = "Get flagged reviews", description = "Retrieves all flagged reviews for admin moderation")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Page<ReviewResponse>> getFlaggedReviews(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.getFlaggedReviews(PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @PutMapping("/admin/{id}/unflag")
    @Operation(summary = "Unflag a review", description = "Removes the flag from a review after admin review")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, String>> unflagReview(@PathVariable Long id) {
        reviewService.unflagReview(id); return ResponseEntity.ok(Map.of("message", "Review unflagged"));
    }

    @DeleteMapping("/admin/{id}")
    @Operation(summary = "Admin delete review", description = "Admin forcefully removes a review from the platform")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> adminDeleteReview(@PathVariable Long id) {
        reviewService.moderateDeleteReview(id); return ResponseEntity.noContent().build();
    }
}
