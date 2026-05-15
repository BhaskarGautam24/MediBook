package com.medibook.appointment.repository;

import com.medibook.appointment.entity.Appointment;
import com.medibook.appointment.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    List<Appointment> findByProviderIdOrderByCreatedAtDesc(Long providerId);
    long countByProviderId(Long providerId);
    long countByProviderIdAndStatus(Long providerId, AppointmentStatus status);
    List<Appointment> findAllByStatus(AppointmentStatus status);
    List<Appointment> findBySlotId(Long slotId);
}
