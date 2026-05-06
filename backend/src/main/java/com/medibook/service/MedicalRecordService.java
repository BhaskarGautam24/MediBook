package com.medibook.service;

import com.medibook.dto.MedicalRecordRequest;
import java.util.List;
import java.util.Map;

public interface MedicalRecordService {
    Map<String, Object> createRecord(MedicalRecordRequest request, Long userId);
    Map<String, Object> getRecordByAppointmentId(Long appointmentId, Long userId, String userRole);
    List<Map<String, Object>> getRecordsByPatientId(Long patientId, Long userId, String userRole);
    List<Map<String, Object>> getRecordsByProviderId(Long providerId, Long userId, String userRole);
    Map<String, Object> updateRecord(Long recordId, MedicalRecordRequest request, Long userId);
    void deleteRecord(Long recordId, Long userId, String userRole);
    List<Map<String, Object>> getFollowUpRecords(Long userId);
    List<Map<String, Object>> getAllRecords(Long userId, String userRole);
}
