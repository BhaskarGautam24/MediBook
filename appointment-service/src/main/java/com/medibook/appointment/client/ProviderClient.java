package com.medibook.appointment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@FeignClient(name = "provider-service")
public interface ProviderClient {
    @GetMapping("/api/internal/providers/{id}")
    Map<String, Object> getProviderById(@PathVariable Long id);

    @GetMapping("/api/internal/providers/by-user/{userId}")
    Map<String, Object> getProviderByUserId(@PathVariable Long userId);
}
