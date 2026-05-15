package com.medibook.record.repository;
import com.medibook.record.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.*;

public interface RecordRepository extends JpaRepository<MedicalRecord, Long> {
    Optional<MedicalRecord> findByAppointmentId(Long appointmentId);
    List<MedicalRecord> findByPatientId(Long patientId);
    List<MedicalRecord> findByProviderId(Long providerId);
    List<MedicalRecord> findByFollowUpDate(LocalDate followUpDate);
}
