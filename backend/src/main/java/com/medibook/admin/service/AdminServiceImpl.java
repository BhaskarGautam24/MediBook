package com.medibook.admin.service;

import com.medibook.admin.dto.*;
import com.medibook.entity.User;
import com.medibook.enums.ProviderStatus;
import com.medibook.service.AppointmentService;
import com.medibook.service.ProviderService;
import com.medibook.service.MedicalRecordService;
import com.medibook.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserService userService;
    private final ProviderService providerService;
    private final AppointmentService appointmentService;
    private final MedicalRecordService medicalRecordService;

    @Override
    public AdminStatsDto getDashboardStats() {
        long totalUsers = userService.getAllUsers().size();
        List<Map<String, Object>> providers = providerService.getAllProviders();
        long totalProviders = providers.size();
        List<Map<String, Object>> appointments = appointmentService.getAllAppointments();
        long totalAppointments = appointments.size();
        long completedAppointments = appointments.stream()
                .filter(a -> "COMPLETED".equals(a.get("status")))
                .count();

        return AdminStatsDto.builder()
                .totalUsers(totalUsers)
                .totalProviders(totalProviders)
                .totalAppointments(totalAppointments)
                .completedAppointments(completedAppointments)
                .build();
    }

    @Override
    public List<AdminUserDto> getAllUsers() {
        return userService.getAllUsers().stream().map(u -> AdminUserDto.builder()
                .id(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .role(u.getRole().name())
                .isActive(u.getIsActive())
                .build()).collect(Collectors.toList());
    }

    @Override
    public void suspendUser(Long id) {
        userService.suspendUser(id);
    }

    @Override
    public void activateUser(Long id) {
        userService.activateUser(id);
    }

    @Override
    public void deleteUser(Long id) {
        userService.deleteUser(id);
    }

    @Override
    public List<AdminProviderDto> getPendingProviders() {
        return providerService.getAllProviders().stream()
                .filter(p -> "PENDING".equals(p.get("status")))
                .map(p -> AdminProviderDto.builder()
                        .id(((Number) p.get("id")).longValue())
                        .name((String) p.get("userName"))
                        .email((String) p.get("userEmail"))
                        .specialization((String) p.get("specialization"))
                        .isVerified((Boolean) p.get("isVerified"))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void verifyProvider(Long id) {
        providerService.approveProvider(id);
    }

    @Override
    public void rejectProvider(Long id) {
        providerService.rejectProvider(id);
    }

    @Override
    public List<AdminAppointmentDto> getAllAppointments() {
        return appointmentService.getAllAppointments().stream().map(a -> AdminAppointmentDto.builder()
                .id(((Number) a.get("id")).longValue())
                .patientName((String) a.get("patientName"))
                .providerName((String) a.get("providerName"))
                .appointmentDate(a.get("slotDate") + " " + a.get("slotStartTime"))
                .status((String) a.get("status"))
                .build()).collect(Collectors.toList());
    }

    @Override
    public List<AdminRecordDto> getAllRecords() {
        // We pass admin user id and role. In real implementation, the current logged-in user id and role should be passed.
        // Assuming admin can just get all without their specific ID check here.
        return medicalRecordService.getAllRecords(null, "ROLE_ADMIN").stream().map(r -> AdminRecordDto.builder()
                .id(((Number) r.get("recordId")).longValue())
                .patientName((String) r.get("patientName"))
                .providerName((String) r.get("providerName"))
                .diagnosis((String) r.get("diagnosis"))
                .prescription((String) r.get("prescription"))
                .createdAt(r.get("createdAt") != null ? r.get("createdAt").toString() : null)
                .build()).collect(Collectors.toList());
    }
}
