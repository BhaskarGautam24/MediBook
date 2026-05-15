package com.medibook.provider.repository;

import com.medibook.provider.entity.Provider;
import com.medibook.provider.enums.ProviderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProviderRepository extends JpaRepository<Provider, Long> {
    Optional<Provider> findByUserId(Long userId);
    List<Provider> findByIsVerifiedTrue();
    List<Provider> findByStatus(ProviderStatus status);
    long countByStatus(ProviderStatus status);
}
