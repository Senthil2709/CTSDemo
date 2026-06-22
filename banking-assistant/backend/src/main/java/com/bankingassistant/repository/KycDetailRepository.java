package com.bankingassistant.repository;

import com.bankingassistant.entity.KycDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface KycDetailRepository extends JpaRepository<KycDetail, UUID> {
    Optional<KycDetail> findByUserId(UUID userId);
}
