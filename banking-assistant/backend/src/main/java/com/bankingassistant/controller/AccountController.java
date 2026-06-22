package com.bankingassistant.controller;

import com.bankingassistant.agent.AccountAgent;
import com.bankingassistant.dto.account.AccountResponse;
import com.bankingassistant.dto.account.SpendingSummaryResponse;
import com.bankingassistant.dto.account.TransactionResponse;
import com.bankingassistant.dto.account.UpgradeEligibilityResponse;
import com.bankingassistant.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountAgent accountAgent;

    @GetMapping
    public ResponseEntity<List<AccountResponse>> myAccounts(@AuthenticationPrincipal SecurityUser principal) {
        return ResponseEntity.ok(accountAgent.getAccounts(principal.getId()));
    }

    @GetMapping("/{accountId}/transactions")
    public ResponseEntity<List<TransactionResponse>> transactions(@PathVariable UUID accountId) {
        return ResponseEntity.ok(accountAgent.getTransactionHistory(accountId));
    }

    @GetMapping("/spending-summary")
    public ResponseEntity<SpendingSummaryResponse> spendingSummary(
            @AuthenticationPrincipal SecurityUser principal,
            @RequestParam(defaultValue = "30") int periodDays) {
        return ResponseEntity.ok(accountAgent.summarizeSpending(principal.getId(), periodDays));
    }

    @GetMapping("/recommend")
    public ResponseEntity<String> recommendAccountType(@AuthenticationPrincipal SecurityUser principal) {
        return ResponseEntity.ok(accountAgent.recommendAccountType(principal.getId()));
    }

    @GetMapping("/upgrade-eligibility")
    public ResponseEntity<UpgradeEligibilityResponse> upgradeEligibility(@AuthenticationPrincipal SecurityUser principal) {
        return ResponseEntity.ok(accountAgent.checkUpgradeEligibility(principal.getId()));
    }

    // Broader access for staff to inspect a specific customer's accounts.
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER', 'BRANCH_ADMIN')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AccountResponse>> accountsForUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(accountAgent.getAccounts(userId));
    }
}
