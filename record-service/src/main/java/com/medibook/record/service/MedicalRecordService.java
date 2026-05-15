package com.medibook.record.service;

import com.medibook.record.client.*;
import com.medibook.record.dto.MedicalRecordRequest;
import com.medibook.record.entity.MedicalRecord;
import com.medibook.record.repository.RecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor @Slf4j
public class MedicalRecordService {

    private final RecordRepository recordRepository;
    private final AuthClient authClient;
    private final ProviderClient providerClient;
    private final AppointmentClient appointmentClient;
    private final NotificationClient notificationClient;

    @Transactional
    public Map<String, Object> createRecord(MedicalRecordRequest request, Long userId) {
        Map<String, Object> appointment = appointmentClient.getAppointmentById(request.getAppointmentId());
        if (!"COMPLETED".equals(appointment.get("status"))) throw new RuntimeException("Appointment must be COMPLETED");
        if (recordRepository.findByAppointmentId(request.getAppointmentId()).isPresent())
            throw new RuntimeException("A medical record already exists for this appointment.");

        Map<String, Object> provider = providerClient.getProviderByUserId(userId);
        Long providerId = Long.valueOf(provider.get("id").toString());
        Long appointmentProviderId = Long.valueOf(appointment.get("providerId").toString());
        if (!appointmentProviderId.equals(providerId)) throw new RuntimeException("Only the provider who handled the appointment can create a record.");

        Long patientId = Long.valueOf(appointment.get("patientId").toString());
        MedicalRecord record = MedicalRecord.builder()
                .appointmentId(request.getAppointmentId()).patientId(patientId).providerId(providerId)
                .diagnosis(request.getDiagnosis()).prescription(request.getPrescription())
                .notes(request.getNotes()).followUpDate(request.getFollowUpDate()).build();
        record = recordRepository.save(record);

        String providerName = (String) provider.getOrDefault("userName", "Doctor");
        try { notificationClient.sendNotification(Map.of("recipientId", patientId, "type", "SYSTEM_MESSAGE", "title", "New Medical Record",
                "message", "Dr. " + providerName + " has added a medical record for your appointment.", "relatedId", record.getRecordId(), "relatedType", "SYSTEM")); }
        catch (Exception e) { log.warn("Notification failed: {}", e.getMessage()); }

        return toMap(record);
    }

    public Map<String, Object> getRecordByAppointmentId(Long appointmentId, Long userId, String userRole) {
        MedicalRecord record = recordRepository.findByAppointmentId(appointmentId).orElseThrow(() -> new RuntimeException("Record not found"));
        validateAccess(record, userId, userRole);
        return toMap(record);
    }

    public List<Map<String, Object>> getRecordsByPatientId(Long patientId, Long userId, String userRole) {
        if ("ROLE_PATIENT".equals(userRole) && !patientId.equals(userId)) throw new RuntimeException("Access denied");
        return recordRepository.findByPatientId(patientId).stream().map(this::toMap).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getRecordsByProviderId(Long providerId, Long userId, String userRole) {
        if ("ROLE_PROVIDER".equals(userRole)) {
            Map<String, Object> p = providerClient.getProviderByUserId(userId);
            if (!providerId.equals(Long.valueOf(p.get("id").toString()))) throw new RuntimeException("Access denied");
        }
        return recordRepository.findByProviderId(providerId).stream().map(this::toMap).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> updateRecord(Long recordId, MedicalRecordRequest request, Long userId) {
        MedicalRecord record = recordRepository.findById(recordId).orElseThrow(() -> new RuntimeException("Record not found"));
        Map<String, Object> provider = providerClient.getProviderByUserId(userId);
        if (!record.getProviderId().equals(Long.valueOf(provider.get("id").toString()))) throw new RuntimeException("Only the original provider can update");
        if (record.getCreatedAt().plusHours(24).isBefore(LocalDateTime.now())) throw new RuntimeException("Editing only within 24h");

        record.setDiagnosis(request.getDiagnosis());
        record.setPrescription(request.getPrescription());
        record.setNotes(request.getNotes());
        record.setFollowUpDate(request.getFollowUpDate());
        record = recordRepository.save(record);

        String providerName = (String) provider.getOrDefault("userName", "Doctor");
        try { notificationClient.sendNotification(Map.of("recipientId", record.getPatientId(), "type", "SYSTEM_MESSAGE", "title", "Medical Record Updated",
                "message", "Dr. " + providerName + " has updated your medical record.", "relatedId", record.getRecordId(), "relatedType", "SYSTEM")); }
        catch (Exception e) { log.warn("Notification failed: {}", e.getMessage()); }
        return toMap(record);
    }

    @Transactional
    public void deleteRecord(Long recordId, Long userId, String userRole) {
        MedicalRecord record = recordRepository.findById(recordId).orElseThrow(() -> new RuntimeException("Record not found"));
        if ("ROLE_PROVIDER".equals(userRole)) {
            Map<String, Object> p = providerClient.getProviderByUserId(userId);
            if (!record.getProviderId().equals(Long.valueOf(p.get("id").toString()))) throw new RuntimeException("Access denied");
        } else if (!"ROLE_ADMIN".equals(userRole)) throw new RuntimeException("Access denied");
        recordRepository.delete(record);
    }

    public List<Map<String, Object>> getFollowUpRecords(Long userId) {
        Map<String, Object> provider = providerClient.getProviderByUserId(userId);
        Long providerId = Long.valueOf(provider.get("id").toString());
        return recordRepository.findByProviderId(providerId).stream().filter(r -> r.getFollowUpDate() != null).map(this::toMap).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getAllRecords(Long userId, String userRole) {
        if (!"ROLE_ADMIN".equals(userRole)) throw new RuntimeException("Admin only");
        return recordRepository.findAll().stream().map(this::toMap).collect(Collectors.toList());
    }

    private void validateAccess(MedicalRecord record, Long userId, String userRole) {
        if ("ROLE_ADMIN".equals(userRole)) return;
        if ("ROLE_PATIENT".equals(userRole) && !record.getPatientId().equals(userId)) throw new RuntimeException("Access denied");
        if ("ROLE_PROVIDER".equals(userRole)) {
            Map<String, Object> p = providerClient.getProviderByUserId(userId);
            if (!record.getProviderId().equals(Long.valueOf(p.get("id").toString()))) throw new RuntimeException("Access denied");
        }
    }

    private Map<String, Object> toMap(MedicalRecord record) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("recordId", record.getRecordId());
        map.put("appointmentId", record.getAppointmentId());
        map.put("patientId", record.getPatientId());
        map.put("providerId", record.getProviderId());

        String patientName = "Patient", providerName = "Doctor";
        try { patientName = (String) authClient.getUserById(record.getPatientId()).getOrDefault("name", "Patient"); } catch (Exception e) {}
        try { providerName = (String) providerClient.getProviderById(record.getProviderId()).getOrDefault("userName", "Doctor"); } catch (Exception e) {}

        map.put("patientName", patientName);
        map.put("providerName", providerName);
        map.put("diagnosis", record.getDiagnosis());
        map.put("prescription", record.getPrescription());
        map.put("notes", record.getNotes());
        map.put("followUpDate", record.getFollowUpDate());
        map.put("createdAt", record.getCreatedAt());
        map.put("updatedAt", record.getUpdatedAt());
        return map;
    }
}
