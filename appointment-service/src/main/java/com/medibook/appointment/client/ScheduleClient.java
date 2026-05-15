package com.medibook.appointment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@FeignClient(name = "schedule-service")
public interface ScheduleClient {
    @GetMapping("/api/internal/slots/{id}")
    Map<String, Object> getSlotById(@PathVariable Long id);

    @PutMapping("/api/internal/slots/{id}/booking")
    Map<String, String> updateBookingStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body);
}
