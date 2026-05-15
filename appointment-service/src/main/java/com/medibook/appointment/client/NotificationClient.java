package com.medibook.appointment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@FeignClient(name = "notification-service")
public interface NotificationClient {
    @PostMapping("/api/internal/notifications/send")
    Map<String, Object> sendNotification(@RequestBody Map<String, Object> notification);
}
