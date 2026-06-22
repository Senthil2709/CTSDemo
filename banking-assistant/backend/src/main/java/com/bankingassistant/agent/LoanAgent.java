package com.bankingassistant.agent;

import com.bankingassistant.dto.agent.AgentRequest;
import com.bankingassistant.dto.agent.AgentResponse;
import com.bankingassistant.dto.loan.EmiCalculationResponse;
import com.bankingassistant.entity.KycDetail;
import com.bankingassistant.entity.LoanType;
import com.bankingassistant.repository.KycDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Module 2 - Loan Agent.
 * Responsible for: evaluating loan eligibility, recommending suitable loan
 * products, calculating EMI / total interest, and checking policy
 * compliance for loan disbursement.
 */
@Component
@RequiredArgsConstructor
public class LoanAgent implements Agent {

    private final KycDetailRepository kycDetailRepository;

    // Base annual interest rates by loan type (policy-driven, simplified for demo).
    private static final Map<LoanType, BigDecimal> BASE_RATES = Map.of(
            LoanType.HOME, new BigDecimal("8.50"),
            LoanType.PERSONAL, new BigDecimal("12.00"),
            LoanType.AUTO, new BigDecimal("9.75")
    );

    @Override
    public String getName() {
        return "LoanAgent";
    }

    /**
     * Evaluates loan eligibility for a customer based on their KYC / credit
     * profile and the requested loan parameters. Returns a map summarizing
     * the decision so it can be reused by both the REST layer and the
     * orchestrator / financial-planning agent.
     */
    public Map<String, Object> evaluateEligibility(UUID userId, LoanType loanType, BigDecimal principalAmount,
                                                     Integer tenureMonths) {
        KycDetail kyc = kycDetailRepository.findByUserId(userId).orElse(null);
        List<String> reasons = new ArrayList<>();
        boolean eligible = true;

        Integer creditScore = kyc != null ? kyc.getCreditScore() : null;
        BigDecimal annualIncome = kyc != null ? kyc.getAnnualIncome() : null;

        if (creditScore == null || creditScore < 650) {
            eligible = false;
            reasons.add("Credit score below the minimum threshold of 650 required for loan approval.");
        } else {
            reasons.add("Credit score of " + creditScore + " meets the minimum requirement.");
        }

        if (annualIncome == null) {
            eligible = false;
            reasons.add("Annual income not on file; please complete KYC before applying.");
        } else {
            // Simple affordability check: estimated EMI should not exceed 50% of monthly income.
            BigDecimal rate = BASE_RATES.getOrDefault(loanType, new BigDecimal("11.00"));
            BigDecimal estimatedEmi = calculateEmi(principalAmount, rate, tenureMonths).getEmi();
            BigDecimal monthlyIncome = annualIncome.divide(new BigDecimal("12"), MathContext.DECIMAL64);
            BigDecimal maxAffordableEmi = monthlyIncome.multiply(new BigDecimal("0.50"));

            if (estimatedEmi.compareTo(maxAffordableEmi) > 0) {
                eligible = false;
                reasons.add(String.format(
                        "Estimated EMI of INR %s exceeds 50%% of your monthly income (INR %s).",
                        estimatedEmi, maxAffordableEmi.setScale(2, RoundingMode.HALF_UP)));
            } else {
                reasons.add("Estimated EMI is within an affordable range relative to your income.");
            }
        }

        if (loanType == LoanType.HOME && principalAmount.compareTo(new BigDecimal("10000000")) > 0) {
            eligible = false;
            reasons.add("Home loan amount exceeds the maximum permissible limit of INR 1,00,00,000 without additional collateral review.");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("eligible", eligible);
        result.put("reasons", reasons);
        result.put("creditScore", creditScore);
        return result;
    }

    /**
     * Recommends a suitable loan product type based on the customer's stated
     * purpose / profile. This is intentionally rule-based and policy-aligned.
     */
    public String recommendLoanProduct(UUID userId, String purpose) {
        String lowerPurpose = purpose == null ? "" : purpose.toLowerCase();

        if (lowerPurpose.contains("home") || lowerPurpose.contains("house") || lowerPurpose.contains("property")) {
            return "A Home Loan would best suit this purpose, offering longer tenures (up to 30 years) " +
                    "and the lowest interest rates among our loan products, typically " + BASE_RATES.get(LoanType.HOME) + "% p.a.";
        }
        if (lowerPurpose.contains("car") || lowerPurpose.contains("vehicle") || lowerPurpose.contains("auto")) {
            return "An Auto Loan is best suited here, with tenures up to 7 years at around " +
                    BASE_RATES.get(LoanType.AUTO) + "% p.a., secured against the vehicle.";
        }
        return "A Personal Loan would suit this purpose, offering quick disbursal with tenures up to 5 years " +
                "at around " + BASE_RATES.get(LoanType.PERSONAL) + "% p.a., though at a relatively higher rate " +
                "since it is typically unsecured.";
    }

    /**
     * Standard reducing-balance EMI calculation:
     * EMI = P * r * (1+r)^n / ((1+r)^n - 1), where r is the monthly rate.
     */
    public EmiCalculationResponse calculateEmi(BigDecimal principal, BigDecimal annualInterestRate, int tenureMonths) {
        MathContext mc = MathContext.DECIMAL64;
        BigDecimal monthlyRate = annualInterestRate.divide(new BigDecimal("1200"), mc);

        BigDecimal emi;
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            emi = principal.divide(new BigDecimal(tenureMonths), mc);
        } else {
            BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
            BigDecimal onePlusRPowN = onePlusR.pow(tenureMonths, mc);
            BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRPowN);
            BigDecimal denominator = onePlusRPowN.subtract(BigDecimal.ONE);
            emi = numerator.divide(denominator, mc);
        }

        emi = emi.setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPayment = emi.multiply(new BigDecimal(tenureMonths)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalInterest = totalPayment.subtract(principal).setScale(2, RoundingMode.HALF_UP);

        return EmiCalculationResponse.builder()
                .emi(emi)
                .totalPayment(totalPayment)
                .totalInterest(totalInterest)
                .build();
    }

    /**
     * Checks whether a proposed loan disbursement complies with core bank
     * policy rules (KYC completion, max loan-to-income ratio, minimum tenure).
     */
    public Map<String, Object> checkPolicyCompliance(UUID userId, LoanType loanType, BigDecimal principalAmount,
                                                       Integer tenureMonths) {
        KycDetail kyc = kycDetailRepository.findByUserId(userId).orElse(null);
        List<String> violations = new ArrayList<>();

        if (kyc == null || kyc.getKycStatus() == null || !"VERIFIED".equalsIgnoreCase(kyc.getKycStatus().name())) {
            violations.add("KYC verification is not complete; disbursement cannot proceed until KYC is VERIFIED.");
        }
        if (tenureMonths != null && tenureMonths < 3) {
            violations.add("Minimum loan tenure under policy is 3 months.");
        }
        if (kyc != null && kyc.getAnnualIncome() != null) {
            BigDecimal maxLoanByIncome = kyc.getAnnualIncome().multiply(new BigDecimal("5"));
            if (principalAmount.compareTo(maxLoanByIncome) > 0) {
                violations.add("Requested principal exceeds 5x annual income cap permitted under lending policy.");
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("compliant", violations.isEmpty());
        result.put("violations", violations);
        return result;
    }

    @Override
    public AgentResponse handle(AgentRequest request) {
        try {
            UUID userId = request.getUserId();
            // Generic chat-driven handling: give an eligibility snapshot using a
            // representative personal-loan scenario, since free text alone may not
            // specify exact loan parameters.
            BigDecimal sampleAmount = new BigDecimal("200000");
            int sampleTenure = 24;
            Map<String, Object> eligibility = evaluateEligibility(userId, LoanType.PERSONAL, sampleAmount, sampleTenure);
            String productAdvice = recommendLoanProduct(userId, request.getMessage());

            boolean eligible = (boolean) eligibility.get("eligible");
            String summary = String.format(
                    "%s Based on a sample personal loan scenario (INR %s over %d months), you are %s for approval. %s",
                    productAdvice, sampleAmount, sampleTenure, eligible ? "currently eligible" : "not currently eligible",
                    String.join(" ", (List<String>) eligibility.get("reasons")));

            Map<String, Object> data = new HashMap<>();
            data.put("eligibility", eligibility);
            data.put("productAdvice", productAdvice);

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
}
