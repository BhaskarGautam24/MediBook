package com.medibook.schedule.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "provider-service")
public interface ProviderClient {

    @GetMapping("/api/internal/providers/by-user/{userId}")
    Map<String, Object> getProviderByUserId(@PathVariable Long userId);

    @GetMapping("/api/internal/providers/{id}")
    Map<String, Object> getProviderById(@PathVariable Long id);
}
