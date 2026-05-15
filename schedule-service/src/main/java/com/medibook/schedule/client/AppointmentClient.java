package com.medibook.schedule.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * Feign client to appointment-service.
 * Used for slot cleanup — notifies appointment-service to handle expired slot appointments.
 */
@FeignClient(name = "appointment-service")
public interface AppointmentClient {

    @PostMapping("/api/internal/appointments/slot-expired/{slotId}")
    Map<String, Object> handleSlotExpired(@PathVariable Long slotId);
}
