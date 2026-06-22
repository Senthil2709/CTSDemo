package com.bankingassistant.service;

import com.bankingassistant.dto.servicerequest.ServiceRequestDecisionRequest;
import com.bankingassistant.dto.servicerequest.ServiceRequestResponse;
import com.bankingassistant.entity.RequestStatus;
import com.bankingassistant.entity.ServiceRequest;
import com.bankingassistant.entity.ServiceRequestType;
import com.bankingassistant.repository.ServiceRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Module 7 - Loan and Service Request Approval Workflow (generic side, for
 * non-loan request types such as ACCOUNT_UPGRADE, CARD, and OTHER).
 * Lifecycle: DRAFT -> PENDING_APPROVAL -> APPROVED / REJECTED -> DISBURSED / ACTIVATED.
 */
@Service
@RequiredArgsConstructor
public class ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;

    @Transactional
    public ServiceRequestResponse createDraft(UUID userId, ServiceRequestType type, UUID referenceId, String payload) {
        ServiceRequest request = ServiceRequest.builder()
                .userId(userId)
                .requestType(type)
                .referenceId(referenceId)
                .status(RequestStatus.DRAFT)
                .payload(payload)
                .build();
        return toResponse(serviceRequestRepository.save(request));
    }

    /** Customers submit a DRAFT request for RM / Branch Admin review. */
    @Transactional
    public ServiceRequestResponse submit(UUID requestId, UUID requestingUserId) {
        ServiceRequest request = getOwned(requestId, requestingUserId);
        if (request.getStatus() != RequestStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT requests can be submitted for approval");
        }
        request.setStatus(RequestStatus.PENDING_APPROVAL);
        return toResponse(serviceRequestRepository.save(request));
    }

    /**
     * Convenience method: customers may also submit a request directly without
     * an intermediate draft step (skips straight to PENDING_APPROVAL).
     */
    @Transactional
    public ServiceRequestResponse createAndSubmit(UUID userId, ServiceRequestType type, UUID referenceId, String payload) {
        ServiceRequest request = ServiceRequest.builder()
                .userId(userId)
                .requestType(type)
                .referenceId(referenceId)
                .status(RequestStatus.PENDING_APPROVAL)
                .payload(payload)
                .build();
        return toResponse(serviceRequestRepository.save(request));
    }

    public List<ServiceRequestResponse> listForUser(UUID userId) {
        return serviceRequestRepository.findByUserId(userId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<ServiceRequestResponse> listPendingApproval() {
        return serviceRequestRepository.findByStatus(RequestStatus.PENDING_APPROVAL).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<ServiceRequestResponse> listAll() {
        return serviceRequestRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public ServiceRequestResponse getById(UUID requestId) {
        return toResponse(serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Service request not found")));
    }

    /** RM / Branch Admin review and update the status (APPROVED, REJECTED, DISBURSED, ACTIVATED). */
    @Transactional
    public ServiceRequestResponse decide(UUID requestId, UUID deciderId, ServiceRequestDecisionRequest decision) {
        ServiceRequest request = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Service request not found"));

        validateTransition(request.getStatus(), decision.getStatus());

        request.setStatus(decision.getStatus());
        request.setDecidedBy(deciderId);
        request.setDecidedAt(LocalDateTime.now());
        request.setDecisionNotes(decision.getNotes());

        return toResponse(serviceRequestRepository.save(request));
    }

    private void validateTransition(RequestStatus current, RequestStatus target) {
        boolean valid = switch (current) {
            case DRAFT -> target == RequestStatus.PENDING_APPROVAL;
            case PENDING_APPROVAL -> target == RequestStatus.APPROVED || target == RequestStatus.REJECTED;
            case APPROVED -> target == RequestStatus.DISBURSED || target == RequestStatus.ACTIVATED;
            default -> false;
        };
        if (!valid) {
            throw new IllegalStateException("Cannot transition service request from " + current + " to " + target);
        }
    }

    private ServiceRequest getOwned(UUID requestId, UUID userId) {
        ServiceRequest request = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Service request not found"));
        if (!request.getUserId().equals(userId)) {
            throw new SecurityException("You do not have access to this service request");
        }
        return request;
    }

    private ServiceRequestResponse toResponse(ServiceRequest request) {
        return ServiceRequestResponse.builder()
                .id(request.getId())
                .userId(request.getUserId())
                .requestType(request.getRequestType().name())
                .referenceId(request.getReferenceId())
                .status(request.getStatus().name())
                .payload(request.getPayload())
                .createdAt(request.getCreatedAt())
                .decisionNotes(request.getDecisionNotes())
                .build();
    }
}
