package com.bankingassistant.controller;

import com.bankingassistant.agent.LoanAgent;
import com.bankingassistant.dto.loan.*;
import com.bankingassistant.entity.RequestStatus;
import com.bankingassistant.security.SecurityUser;
import com.bankingassistant.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;
    private final LoanAgent loanAgent;

    @PostMapping("/apply")
    public ResponseEntity<LoanResponse> apply(@AuthenticationPrincipal SecurityUser principal,
                                               @Valid @RequestBody LoanApplicationRequest request) {
        return ResponseEntity.ok(loanService.apply(principal.getId(), request));
    }

    @GetMapping
    public ResponseEntity<List<LoanResponse>> myLoans(@AuthenticationPrincipal SecurityUser principal) {
        return ResponseEntity.ok(loanService.listForUser(principal.getId()));
    }

    @GetMapping("/{loanId}")
    public ResponseEntity<LoanResponse> getLoan(@PathVariable UUID loanId) {
        return ResponseEntity.ok(loanService.getById(loanId));
    }

    @PostMapping("/emi-calculator")
    public ResponseEntity<EmiCalculationResponse> calculateEmi(@Valid @RequestBody EmiCalculationRequest request) {
        return ResponseEntity.ok(loanAgent.calculateEmi(
                request.getPrincipalAmount(), request.getAnnualInterestRate(), request.getTenureMonths()));
    }

    @GetMapping("/eligibility")
    public ResponseEntity<Map<String, Object>> checkEligibility(
            @AuthenticationPrincipal SecurityUser principal,
            @RequestParam com.bankingassistant.entity.LoanType loanType,
            @RequestParam java.math.BigDecimal principalAmount,
            @RequestParam Integer tenureMonths) {
        return ResponseEntity.ok(loanAgent.evaluateEligibility(principal.getId(), loanType, principalAmount, tenureMonths));
    }

    @GetMapping("/recommend")
    public ResponseEntity<String> recommendProduct(@AuthenticationPrincipal SecurityUser principal,
                                                     @RequestParam String purpose) {
        return ResponseEntity.ok(loanAgent.recommendLoanProduct(principal.getId(), purpose));
    }

    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER', 'BRANCH_ADMIN')")
    @GetMapping("/pending")
    public ResponseEntity<List<LoanResponse>> pending() {
        return ResponseEntity.ok(loanService.listByStatus(RequestStatus.PENDING_APPROVAL));
    }

    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER', 'BRANCH_ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<LoanResponse>> all() {
        return ResponseEntity.ok(loanService.listAll());
    }

    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER', 'BRANCH_ADMIN')")
    @PostMapping("/{loanId}/decision")
    public ResponseEntity<LoanResponse> decide(@AuthenticationPrincipal SecurityUser principal,
                                                @PathVariable UUID loanId,
                                                @Valid @RequestBody LoanDecisionRequest request) {
        return ResponseEntity.ok(loanService.decide(loanId, principal.getId(), request));
    }
}
