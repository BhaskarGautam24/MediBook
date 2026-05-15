package com.medibook.payment.repository;

import com.medibook.payment.entity.Payment;
import com.medibook.payment.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByAppointmentId(Long appointmentId);
    List<Payment> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    List<Payment> findByProviderIdOrderByCreatedAtDesc(Long providerId);
    List<Payment> findByProviderIdAndStatus(Long providerId, PaymentStatus status);
    List<Payment> findByProviderIdAndStatusAndPaidAtBetween(Long providerId, PaymentStatus status, LocalDateTime start, LocalDateTime end);
    List<Payment> findByProviderIdAndStatusAndRefundedAtBetween(Long providerId, PaymentStatus status, LocalDateTime start, LocalDateTime end);
    List<Payment> findByStatus(PaymentStatus status);
}
