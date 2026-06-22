package com.bankingassistant.controller;

import com.bankingassistant.dto.servicerequest.ServiceRequestDecisionRequest;
import com.bankingassistant.dto.servicerequest.ServiceRequestResponse;
import com.bankingassistant.entity.ServiceRequestType;
import com.bankingassistant.security.SecurityUser;
import com.bankingassistant.service.ServiceRequestService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/service-requests")
@RequiredArgsConstructor
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;

    @PostMapping
    public ResponseEntity<ServiceRequestResponse> create(@AuthenticationPrincipal SecurityUser principal,
                                                            @Valid @RequestBody CreateRequest request) {
        return ResponseEntity.ok(serviceRequestService.createAndSubmit(
                principal.getId(), request.getRequestType(), request.getReferenceId(), request.getPayload()));
    }

    @PostMapping("/draft")
    public ResponseEntity<ServiceRequestResponse> createDraft(@AuthenticationPrincipal SecurityUser principal,
                                                                 @Valid @RequestBody CreateRequest request) {
        return ResponseEntity.ok(serviceRequestService.createDraft(
                principal.getId(), request.getRequestType(), request.getReferenceId(), request.getPayload()));
    }

    @PostMapping("/{requestId}/submit")
    public ResponseEntity<ServiceRequestResponse> submit(@AuthenticationPrincipal SecurityUser principal,
                                                            @PathVariable UUID requestId) {
        return ResponseEntity.ok(serviceRequestService.submit(requestId, principal.getId()));
    }

    @GetMapping
    public ResponseEntity<List<ServiceRequestResponse>> myRequests(@AuthenticationPrincipal SecurityUser principal) {
        return ResponseEntity.ok(serviceRequestService.listForUser(principal.getId()));
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ServiceRequestResponse> getById(@PathVariable UUID requestId) {
        return ResponseEntity.ok(serviceRequestService.getById(requestId));
    }

    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER', 'BRANCH_ADMIN')")
    @GetMapping("/pending")
    public ResponseEntity<List<ServiceRequestResponse>> pending() {
        return ResponseEntity.ok(serviceRequestService.listPendingApproval());
    }

    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER', 'BRANCH_ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<ServiceRequestResponse>> all() {
        return ResponseEntity.ok(serviceRequestService.listAll());
    }

    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER', 'BRANCH_ADMIN')")
    @PostMapping("/{requestId}/decision")
    public ResponseEntity<ServiceRequestResponse> decide(@AuthenticationPrincipal SecurityUser principal,
                                                            @PathVariable UUID requestId,
                                                            @Valid @RequestBody ServiceRequestDecisionRequest request) {
        return ResponseEntity.ok(serviceRequestService.decide(requestId, principal.getId(), request));
    }

    @Data
    public static class CreateRequest {
        @NotNull
        private ServiceRequestType requestType;
        private UUID referenceId;
        private String payload;
    }
}
