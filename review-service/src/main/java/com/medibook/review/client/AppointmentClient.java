package com.medibook.review.client;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@FeignClient(name = "appointment-service")
public interface AppointmentClient {
    @GetMapping("/api/internal/appointments/{id}") Map<String, Object> getAppointmentById(@PathVariable Long id);
}
