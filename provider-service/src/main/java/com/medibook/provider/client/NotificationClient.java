package com.medibook.provider.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * Feign client to notification-service.
 * Used when admin approves/rejects a provider.
 */
@FeignClient(name = "notification-service")
public interface NotificationClient {

    @PostMapping("/api/internal/notifications/send")
    Map<String, Object> sendNotification(@RequestBody Map<String, Object> notification);
}
