package com.medibook.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@FeignClient(name = "auth-service")
public interface AuthClient {
    @GetMapping("/api/internal/users/{id}")
    Map<String, Object> getUserById(@PathVariable Long id);
}
