package com.medibook.repository;

import com.medibook.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecordRepository extends JpaRepository<MedicalRecord, Long> {
    Optional<MedicalRecord> findByAppointmentId(Long appointmentId);
    List<MedicalRecord> findByPatientId(Long patientId);
    List<MedicalRecord> findByProviderId(Long providerId);
    List<MedicalRecord> findByFollowUpDate(LocalDate followUpDate);
}
