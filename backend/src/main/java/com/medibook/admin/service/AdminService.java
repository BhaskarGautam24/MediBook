package com.medibook.admin.service;

import com.medibook.admin.dto.*;
import java.util.List;

public interface AdminService {
    AdminStatsDto getDashboardStats();
    
    List<AdminUserDto> getAllUsers();
    void suspendUser(Long id);
    void activateUser(Long id);
    void deleteUser(Long id);
    
    List<AdminProviderDto> getPendingProviders();
    void verifyProvider(Long id);
    void rejectProvider(Long id);
    
    List<AdminAppointmentDto> getAllAppointments();
    
    List<AdminRecordDto> getAllRecords();
}
