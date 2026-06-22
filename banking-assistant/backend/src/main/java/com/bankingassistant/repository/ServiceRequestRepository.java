package com.bankingassistant.repository;

import com.bankingassistant.entity.RequestStatus;
import com.bankingassistant.entity.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, UUID> {
    List<ServiceRequest> findByUserId(UUID userId);
    List<ServiceRequest> findByStatus(RequestStatus status);
    List<ServiceRequest> findByStatusIn(List<RequestStatus> statuses);
}
