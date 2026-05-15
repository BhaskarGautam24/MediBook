package com.medibook.review.repository;

import com.medibook.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByProviderId(Long providerId, Pageable pageable);
    List<Review> findByProviderId(Long providerId);
    Page<Review> findByPatientId(Long patientId, Pageable pageable);
    Optional<Review> findByAppointmentId(Long appointmentId);
    boolean existsByAppointmentId(Long appointmentId);
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.providerId = :providerId AND r.isFlagged = false")
    Double avgRatingByProviderId(@Param("providerId") Long providerId);
    @Query("SELECT COUNT(r) FROM Review r WHERE r.providerId = :providerId AND r.isFlagged = false")
    long countActiveByProviderId(@Param("providerId") Long providerId);
    long countByProviderId(Long providerId);
    Page<Review> findByIsFlaggedTrue(Pageable pageable);
}
