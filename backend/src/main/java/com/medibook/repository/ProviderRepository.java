package com.medibook.repository;

import com.medibook.entity.Provider;
import com.medibook.enums.ProviderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, Long> {
    Optional<Provider> findByUserId(Long userId);
    List<Provider> findByIsVerifiedTrue();
    List<Provider> findByStatus(ProviderStatus status);
    long countByStatus(ProviderStatus status);
}
