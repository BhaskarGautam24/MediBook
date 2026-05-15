package com.medibook.provider.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * Feign client to auth-service for user details.
 * Used to get user name/email when building provider API responses.
 */
@FeignClient(name = "auth-service")
public interface AuthClient {

    @GetMapping("/api/internal/users/{id}")
    Map<String, Object> getUserById(@PathVariable Long id);
}
