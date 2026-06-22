package com.bankingassistant.agent;

import com.bankingassistant.dto.account.AccountResponse;
import com.bankingassistant.dto.account.SpendingSummaryResponse;
import com.bankingassistant.dto.account.TransactionResponse;
import com.bankingassistant.dto.account.UpgradeEligibilityResponse;
import com.bankingassistant.dto.agent.AgentRequest;
import com.bankingassistant.dto.agent.AgentResponse;
import com.bankingassistant.entity.Account;
import com.bankingassistant.entity.AccountTier;
import com.bankingassistant.entity.KycDetail;
import com.bankingassistant.entity.Transaction;
import com.bankingassistant.entity.User;
import com.bankingassistant.repository.AccountRepository;
import com.bankingassistant.repository.KycDetailRepository;
import com.bankingassistant.repository.TransactionRepository;
import com.bankingassistant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Module 2 - Account Agent.
 * Responsible for: retrieving balances/transaction history, recommending
 * account types, checking account-tier upgrade eligibility, and summarizing
 * account activity / spending patterns.
 */
@Component
@RequiredArgsConstructor
public class AccountAgent implements Agent {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final KycDetailRepository kycDetailRepository;

    @Override
    public String getName() {
        return "AccountAgent";
    }

    public List<AccountResponse> getAccounts(UUID userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(this::toAccountResponse)
                .collect(Collectors.toList());
    }

    public List<TransactionResponse> getTransactionHistory(UUID accountId) {
        return transactionRepository.findByAccountIdOrderByTransactedAtDesc(accountId).stream()
                .map(this::toTransactionResponse)
                .collect(Collectors.toList());
    }

    public SpendingSummaryResponse summarizeSpending(UUID userId, int periodDays) {
        LocalDateTime since = LocalDateTime.now().minusDays(periodDays);
        List<Account> accounts = accountRepository.findByUserId(userId);

        BigDecimal totalCredits = BigDecimal.ZERO;
        BigDecimal totalDebits = BigDecimal.ZERO;
        Map<String, BigDecimal> byCategory = new HashMap<>();

        for (Account account : accounts) {
            List<Transaction> txns = transactionRepository
                    .findByAccountIdAndTransactedAtAfterOrderByTransactedAtDesc(account.getId(), since);
            for (Transaction t : txns) {
                if (t.getType() == com.bankingassistant.entity.TransactionType.CREDIT) {
                    totalCredits = totalCredits.add(t.getAmount());
                } else {
                    totalDebits = totalDebits.add(t.getAmount());
                    String category = t.getCategory() == null ? "OTHER" : t.getCategory();
                    byCategory.merge(category, t.getAmount(), BigDecimal::add);
                }
            }
        }

        return SpendingSummaryResponse.builder()
                .totalCredits(totalCredits)
                .totalDebits(totalDebits)
                .spendingByCategory(byCategory)
                .periodDays(periodDays)
                .build();
    }

    public String recommendAccountType(UUID userId) {
        KycDetail kyc = kycDetailRepository.findByUserId(userId).orElse(null);
        List<Account> existing = accountRepository.findByUserId(userId);

        boolean hasCurrentAccount = existing.stream()
                .anyMatch(a -> a.getAccountType() == com.bankingassistant.entity.AccountType.CURRENT);

        if (kyc != null && "SELF_EMPLOYED".equalsIgnoreCase(kyc.getEmploymentStatus()) && !hasCurrentAccount) {
            return "Based on your self-employed profile, a Current Account would suit your business " +
                    "transaction volumes better, alongside your existing Savings Account.";
        }

        boolean hasFixedDeposit = existing.stream()
                .anyMatch(a -> a.getAccountType() == com.bankingassistant.entity.AccountType.FIXED_DEPOSIT);
        if (kyc != null && kyc.getAnnualIncome() != null
                && kyc.getAnnualIncome().compareTo(new BigDecimal("600000")) > 0 && !hasFixedDeposit) {
            return "With your income profile, opening a Fixed Deposit account could help you earn better " +
                    "interest on idle savings while keeping funds relatively liquid.";
        }

        return "A standard Savings Account with UPI and debit card access is a good fit based on your current profile.";
    }

    public UpgradeEligibilityResponse checkUpgradeEligibility(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow();
        KycDetail kyc = kycDetailRepository.findByUserId(userId).orElse(null);
        SpendingSummaryResponse spending = summarizeSpending(userId, 90);

        List<String> reasons = new ArrayList<>();
        AccountTier recommended = user.getAccountTier();

        BigDecimal avgQuarterlyInflow = spending.getTotalCredits();

        if (kyc != null && kyc.getCreditScore() != null && kyc.getCreditScore() >= 750
                && avgQuarterlyInflow.compareTo(new BigDecimal("300000")) > 0) {
            recommended = AccountTier.PLATINUM;
            reasons.add("Excellent credit score (" + kyc.getCreditScore() + ") and strong quarterly inflows");
        } else if (avgQuarterlyInflow.compareTo(new BigDecimal("150000")) > 0) {
            recommended = AccountTier.GOLD;
            reasons.add("Consistent quarterly inflows above INR 1,50,000");
        } else if (avgQuarterlyInflow.compareTo(new BigDecimal("50000")) > 0) {
            recommended = AccountTier.SILVER;
            reasons.add("Quarterly inflows above INR 50,000");
        } else {
            reasons.add("Current activity levels match the Standard tier");
        }

        boolean eligible = recommended.ordinal() > user.getAccountTier().ordinal();

        return UpgradeEligibilityResponse.builder()
                .currentTier(user.getAccountTier().name())
                .recommendedTier(recommended.name())
                .eligibleForUpgrade(eligible)
                .reasons(reasons)
                .build();
    }

    @Override
    public AgentResponse handle(AgentRequest request) {
        try {
            List<AccountResponse> accounts = getAccounts(request.getUserId());
            BigDecimal totalBalance = accounts.stream()
                    .map(AccountResponse::getBalance)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);

            SpendingSummaryResponse spending = summarizeSpending(request.getUserId(), 30);

            String summary = String.format(
                    "You hold %d account(s) with a combined balance of INR %s. In the last 30 days you " +
                            "received INR %s and spent INR %s.",
                    accounts.size(), totalBalance, spending.getTotalCredits(), spending.getTotalDebits());

            Map<String, Object> data = new HashMap<>();
            data.put("accounts", accounts);
            data.put("spending", spending);

            return AgentResponse.builder()
                    .agentName(getName())
                    .summary(summary)
                    .data(data)
                    .success(true)
                    .build();
        } catch (Exception ex) {
            return AgentResponse.builder()
                    .agentName(getName())
                    .success(false)
                    .error(ex.getMessage())
                    .build();
        }
    }

    private AccountResponse toAccountResponse(Account a) {
        return AccountResponse.builder()
                .id(a.getId())
                .accountNumber(a.getAccountNumber())
                .accountType(a.getAccountType().name())
                .balance(a.getBalance())
                .currency(a.getCurrency())
                .status(a.getStatus().name())
                .build();
    }

    private TransactionResponse toTransactionResponse(Transaction t) {
        return TransactionResponse.builder()
                .type(t.getType().name())
                .category(t.getCategory())
                .amount(t.getAmount())
                .description(t.getDescription())
                .balanceAfter(t.getBalanceAfter())
                .transactedAt(t.getTransactedAt())
                .build();
    }
}
