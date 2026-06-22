package com.bankingassistant.controller;

import com.bankingassistant.dto.loan.LoanResponse;
import com.bankingassistant.dto.servicerequest.ServiceRequestResponse;
import com.bankingassistant.entity.RequestStatus;
import com.bankingassistant.service.LoanService;
import com.bankingassistant.service.ServiceRequestService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Dashboard endpoint for Relationship Managers / Branch Admins: aggregates
 * every PENDING_APPROVAL loan and generic service request in one call so
 * staff don't need to poll two separate endpoints.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER', 'BRANCH_ADMIN')")
public class AdminController {

    private final LoanService loanService;
    private final ServiceRequestService serviceRequestService;

    @GetMapping("/approval-queue")
    public ResponseEntity<ApprovalQueueResponse> approvalQueue() {
        List<LoanResponse> pendingLoans = loanService.listByStatus(RequestStatus.PENDING_APPROVAL);
        List<ServiceRequestResponse> pendingRequests = serviceRequestService.listPendingApproval();

        return ResponseEntity.ok(ApprovalQueueResponse.builder()
                .pendingLoans(pendingLoans)
                .pendingServiceRequests(pendingRequests)
                .totalPending(pendingLoans.size() + pendingRequests.size())
                .build());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApprovalQueueResponse {
        private List<LoanResponse> pendingLoans;
        private List<ServiceRequestResponse> pendingServiceRequests;
        private int totalPending;
    }
}
