package com.medibook.service.impl;

import com.medibook.dto.MedicalRecordRequest;
import com.medibook.entity.Appointment;
import com.medibook.entity.MedicalRecord;
import com.medibook.entity.Provider;
import com.medibook.enums.AppointmentStatus;
import com.medibook.enums.NotificationType;
import com.medibook.enums.RelatedType;
import com.medibook.repository.AppointmentRepository;
import com.medibook.repository.ProviderRepository;
import com.medibook.repository.RecordRepository;
import com.medibook.service.MedicalRecordService;
import com.medibook.service.NotificationService;
import com.medibook.service.ReminderSchedulingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final RecordRepository recordRepository;
    private final AppointmentRepository appointmentRepository;
    private final ProviderRepository providerRepository;
    private final NotificationService notificationService;
    private final ReminderSchedulingService reminderSchedulingService;

    @Override
    @Transactional
    public Map<String, Object> createRecord(MedicalRecordRequest request, Long userId) {
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new RuntimeException("Cannot create record. Appointment must be COMPLETED.");
        }

        if (recordRepository.findByAppointmentId(appointment.getId()).isPresent()) {
            throw new RuntimeException("A medical record already exists for this appointment.");
        }

        Provider provider = providerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Provider profile not found"));

        if (!appointment.getProvider().getId().equals(provider.getId())) {
            throw new RuntimeException("Only the provider who handled the appointment can create a record for it.");
        }

        MedicalRecord record = MedicalRecord.builder()
                .appointment(appointment)
                .patientId(appointment.getPatient().getId())
                .providerId(provider.getId())
                .diagnosis(request.getDiagnosis())
                .prescription(request.getPrescription())
                .notes(request.getNotes())
                .followUpDate(request.getFollowUpDate())
                .build();

        record = recordRepository.save(record);

        if (record.getFollowUpDate() != null) {
            reminderSchedulingService.scheduleFollowUpReminder(
                    record.getRecordId(),
                    appointment.getPatient().getId(),
                    appointment.getId(),
                    record.getFollowUpDate()
            );
            log.info("Follow-up reminder scheduled for Patient {} on {}", appointment.getPatient().getName(), record.getFollowUpDate());
        }

        notificationService.createAndSendNotification(
                appointment.getPatient().getId(),
                NotificationType.NEW_MEDICAL_RECORD,
                "New Medical Record Available",
                String.format("Dr. %s has added a medical record for your appointment on %s", provider.getUser().getName(), appointment.getSlot() != null ? appointment.getSlot().getDate() : appointment.getSlotDate()),
                record.getRecordId(),
                RelatedType.RECORD
        );

        return toMap(record);
    }

    @Override
    public Map<String, Object> getRecordByAppointmentId(Long appointmentId, Long userId, String userRole) {
        MedicalRecord record = recordRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new RuntimeException("Record not found for this appointment"));
        
        validateAccess(record, userId, userRole);
        return toMap(record);
    }

    @Override
    public List<Map<String, Object>> getRecordsByPatientId(Long patientId, Long userId, String userRole) {
        if ("ROLE_PATIENT".equals(userRole) && !patientId.equals(userId)) {
            throw new RuntimeException("Access denied. You can only view your own records.");
        }
        
        return recordRepository.findByPatientId(patientId).stream()
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getRecordsByProviderId(Long providerId, Long userId, String userRole) {
        if ("ROLE_PROVIDER".equals(userRole)) {
            Provider provider = providerRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Provider profile not found"));
            if (!providerId.equals(provider.getId())) {
                throw new RuntimeException("Access denied. You can only view your own records.");
            }
        }
        
        return recordRepository.findByProviderId(providerId).stream()
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Map<String, Object> updateRecord(Long recordId, MedicalRecordRequest request, Long userId) {
        MedicalRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        Provider provider = providerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Provider profile not found"));

        if (!record.getProviderId().equals(provider.getId())) {
            throw new RuntimeException("Only the original provider can update this record.");
        }

        // Limit edit window to 24 hours
        LocalDateTime now = LocalDateTime.now();
        if (record.getCreatedAt().plusHours(24).isBefore(now)) {
            throw new RuntimeException("Editing is only allowed within 24 hours of record creation.");
        }

        record.setDiagnosis(request.getDiagnosis());
        record.setPrescription(request.getPrescription());
        record.setNotes(request.getNotes());
        record.setFollowUpDate(request.getFollowUpDate());

        record = recordRepository.save(record);

        // Schedule/update follow-up reminder if follow-up date changed
        if (record.getFollowUpDate() != null) {
            reminderSchedulingService.scheduleFollowUpReminder(
                    record.getRecordId(),
                    record.getPatientId(),
                    record.getAppointment().getId(),
                    record.getFollowUpDate()
            );
            log.info("Follow-up reminder updated for record {}", record.getRecordId());
        }

        notificationService.createAndSendNotification(
                record.getPatientId(),
                NotificationType.MEDICAL_RECORD_UPDATED,
                "Medical Record Updated",
                String.format("Dr. %s has updated your medical record.", provider.getUser().getName()),
                record.getRecordId(),
                RelatedType.RECORD
        );

        return toMap(record);
    }

    @Override
    @Transactional
    public void deleteRecord(Long recordId, Long userId, String userRole) {
        MedicalRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        if ("ROLE_PROVIDER".equals(userRole)) {
            Provider provider = providerRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Provider profile not found"));
            if (!record.getProviderId().equals(provider.getId())) {
                throw new RuntimeException("Access denied. You can only delete your own records.");
            }
        } else if (!"ROLE_ADMIN".equals(userRole)) {
             throw new RuntimeException("Access denied.");
        }

        recordRepository.delete(record);
    }

    @Override
    public List<Map<String, Object>> getFollowUpRecords(Long userId) {
        Provider provider = providerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Provider profile not found"));

        // For simplicity, retrieving all follow-ups for this provider, ordered by date
        // Note: the prompt asks for getFollowUpRecords without much specific detail, so returning provider's records that have a follow up date.
        return recordRepository.findByProviderId(provider.getId()).stream()
                .filter(r -> r.getFollowUpDate() != null)
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getAllRecords(Long userId, String userRole) {
        if (!"ROLE_ADMIN".equals(userRole)) {
            throw new RuntimeException("Access denied. Admin only.");
        }
        return recordRepository.findAll().stream()
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    private void validateAccess(MedicalRecord record, Long userId, String userRole) {
        if ("ROLE_ADMIN".equals(userRole)) return;
        
        if ("ROLE_PATIENT".equals(userRole)) {
            if (!record.getPatientId().equals(userId)) {
                throw new RuntimeException("Access denied. You can only view your own records.");
            }
        } else if ("ROLE_PROVIDER".equals(userRole)) {
            Provider provider = providerRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Provider profile not found"));
            if (!record.getProviderId().equals(provider.getId())) {
                throw new RuntimeException("Access denied. You can only view records you created.");
            }
        } else {
             throw new RuntimeException("Access denied.");
        }
    }

    private Map<String, Object> toMap(MedicalRecord record) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("recordId", record.getRecordId());
        map.put("appointmentId", record.getAppointment().getId());
        map.put("patientId", record.getPatientId());
        map.put("patientName", record.getAppointment().getPatient().getName());
        map.put("providerId", record.getProviderId());
        map.put("providerName", record.getAppointment().getProvider().getUser().getName());
        map.put("diagnosis", record.getDiagnosis());
        map.put("prescription", record.getPrescription());
        map.put("notes", record.getNotes());
        map.put("followUpDate", record.getFollowUpDate());
        map.put("createdAt", record.getCreatedAt());
        map.put("updatedAt", record.getUpdatedAt());
        return map;
    }
}
