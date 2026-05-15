package com.medibook.notification.controller;

import com.medibook.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Hidden;

import java.util.Map;

/**
 * Internal API — called by other microservices to send notifications.
 */
@Hidden
@RestController
@RequestMapping("/api/internal/notifications")
@RequiredArgsConstructor
public class InternalNotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendNotification(@RequestBody Map<String, Object> data) {
        notificationService.createAndSendNotification(
                Long.valueOf(data.get("recipientId").toString()),
                (String) data.get("type"),
                (String) data.get("title"),
                (String) data.get("message"),
                data.get("relatedId") != null ? Long.valueOf(data.get("relatedId").toString()) : null,
                (String) data.get("relatedType")
        );
        return ResponseEntity.ok(Map.of("status", "sent"));
    }
}
