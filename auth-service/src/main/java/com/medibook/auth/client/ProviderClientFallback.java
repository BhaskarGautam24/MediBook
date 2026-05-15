package com.medibook.auth.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Fallback for ProviderClient when provider-service is unavailable.
 * Registration succeeds even if provider profile creation fails — it can be retried.
 */
@Component
@Slf4j
public class ProviderClientFallback implements ProviderClient {

    @Override
    public Map<String, Object> createProvider(Map<String, Object> providerData) {
        log.warn("Provider service unavailable. Provider profile will be created when service comes online.");
        return Map.of("created", false, "message", "Provider service unavailable");
    }
}
