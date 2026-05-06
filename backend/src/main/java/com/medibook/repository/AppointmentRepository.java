package com.medibook.repository;

import com.medibook.entity.Appointment;
import com.medibook.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    List<Appointment> findByProviderIdOrderByCreatedAtDesc(Long providerId);
    long countByProviderId(Long providerId);
    long countByProviderIdAndStatus(Long providerId, AppointmentStatus status);
    List<Appointment> findAllByStatus(AppointmentStatus status);
    List<Appointment> findBySlotId(Long slotId);
}
