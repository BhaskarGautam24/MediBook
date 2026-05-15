package com.medibook.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * Feign client to call provider-service.
 * Used during registration to create a provider profile when role=PROVIDER.
 */
@FeignClient(name = "provider-service", fallback = ProviderClientFallback.class)
public interface ProviderClient {

    @PostMapping("/api/internal/providers")
    Map<String, Object> createProvider(@RequestBody Map<String, Object> providerData);
}
