package com.medibook.repository;

import com.medibook.entity.Payment;
import com.medibook.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Find payment for a specific appointment
    Optional<Payment> findByAppointmentId(Long appointmentId);

    // Patient's payment history (newest first)
    List<Payment> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    // All payments for a provider (for earnings)
    List<Payment> findByProviderIdOrderByCreatedAtDesc(Long providerId);

    // Provider payments filtered by status
    List<Payment> findByProviderIdAndStatus(Long providerId, PaymentStatus status);

    // Provider paid payments in a date range (for monthly/daily earnings)
    List<Payment> findByProviderIdAndStatusAndPaidAtBetween(
            Long providerId, PaymentStatus status,
            LocalDateTime start, LocalDateTime end);

    // All payments by status (admin)
    List<Payment> findByStatus(PaymentStatus status);
}
