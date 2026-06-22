package com.bankingassistant.agent;

import com.bankingassistant.dto.agent.AgentRequest;
import com.bankingassistant.dto.agent.AgentResponse;
import com.bankingassistant.dto.financialplan.FinancialPlanRequest;
import com.bankingassistant.dto.financialplan.FinancialPlanResponse;
import com.bankingassistant.dto.financialplan.MonthlyScheduleItem;
import com.bankingassistant.dto.loan.EmiCalculationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Module 2 - Financial Planning Agent / Module 6 - AI Financial Plan Generation.
 * Generates personalised savings & investment plans, suggests suitable
 * financial products, provides budget breakdowns and goal-based
 * projections, and keeps recommendations policy-compliant and
 * risk-appropriate.
 */
@Component
@RequiredArgsConstructor
public class FinancialPlanningAgent implements Agent {

    private final LoanAgent loanAgent;

    private static final MathContext MC = MathContext.DECIMAL64;

    @Override
    public String getName() {
        return "FinancialPlanningAgent";
    }

    public FinancialPlanResponse generateFinancialPlan(FinancialPlanRequest request) {
        BigDecimal surplus = request.getMonthlyIncome()
                .subtract(request.getMonthlyExpenses())
                .subtract(request.getExistingMonthlyEmi() == null ? BigDecimal.ZERO : request.getExistingMonthlyEmi());
        if (surplus.compareTo(BigDecimal.ZERO) < 0) {
            surplus = BigDecimal.ZERO;
        }

        String risk = request.getRiskAppetite() == null ? "MODERATE" : request.getRiskAppetite().toUpperCase();
        BigDecimal annualReturnRate = annualReturnFor(risk);
        BigDecimal monthlyRate = annualReturnRate.divide(new BigDecimal("1200"), MC);

        BigDecimal recommendedMonthlySavings = surplus;
        Integer estimatedGoalAchievementMonths = request.getTimelineMonths();
        boolean goalAchievable = true;

        if (request.getTargetAmount() != null && request.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            // Required monthly contribution (ordinary annuity) to hit target within the stated timeline.
            BigDecimal requiredMonthly = requiredMonthlyContribution(
                    request.getTargetAmount(), monthlyRate, request.getTimelineMonths());

            if (requiredMonthly.compareTo(surplus) <= 0) {
                recommendedMonthlySavings = requiredMonthly.max(BigDecimal.ZERO);
                estimatedGoalAchievementMonths = request.getTimelineMonths();
                goalAchievable = true;
            } else {
                // Surplus isn't enough to hit the goal in the requested timeline;
                // recommend saving the full surplus and project how long it would actually take.
                recommendedMonthlySavings = surplus;
                estimatedGoalAchievementMonths = monthsToReachTarget(
                        request.getTargetAmount(), monthlyRate, surplus, request.getTimelineMonths() * 4);
                goalAchievable = estimatedGoalAchievementMonths != null
                        && estimatedGoalAchievementMonths <= request.getTimelineMonths();
            }
        }

        List<MonthlyScheduleItem> schedule = buildSchedule(recommendedMonthlySavings, monthlyRate, request.getTimelineMonths());

        List<String> investmentProducts = investmentProductsFor(risk, request.getGoal());
        List<String> loanOptions = loanOptionsFor(request);
        List<String> taxNotes = regulatoryAndTaxNotesFor(risk);

        String narrative = buildNarrative(request, surplus, recommendedMonthlySavings, risk, goalAchievable,
                estimatedGoalAchievementMonths);

        return FinancialPlanResponse.builder()
                .recommendedMonthlySavings(recommendedMonthlySavings.setScale(2, RoundingMode.HALF_UP))
                .monthlySurplus(surplus.setScale(2, RoundingMode.HALF_UP))
                .suggestedInvestmentProducts(investmentProducts)
                .loanOptionsIfApplicable(loanOptions)
                .monthlySchedule(schedule)
                .estimatedGoalAchievementMonths(estimatedGoalAchievementMonths)
                .goalAchievableInTimeline(goalAchievable)
                .regulatoryAndTaxNotes(taxNotes)
                .narrativeSummary(narrative)
                .build();
    }

    private BigDecimal annualReturnFor(String risk) {
        switch (risk) {
            case "CONSERVATIVE":
                return new BigDecimal("6.0");
            case "AGGRESSIVE":
                return new BigDecimal("13.0");
            default:
                return new BigDecimal("9.0");
        }
    }

    /** Solves PMT for a target future value via the ordinary annuity formula. */
    private BigDecimal requiredMonthlyContribution(BigDecimal targetAmount, BigDecimal monthlyRate, int months) {
        if (months <= 0) {
            return targetAmount;
        }
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return targetAmount.divide(new BigDecimal(months), MC);
        }
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal factor = onePlusR.pow(months, MC).subtract(BigDecimal.ONE);
        return targetAmount.multiply(monthlyRate, MC).divide(factor, MC);
    }

    /** Estimates how many months a fixed contribution takes to reach the target, capped at maxMonths. */
    private Integer monthsToReachTarget(BigDecimal targetAmount, BigDecimal monthlyRate, BigDecimal contribution,
                                         int maxMonths) {
        if (contribution.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        BigDecimal corpus = BigDecimal.ZERO;
        for (int month = 1; month <= maxMonths; month++) {
            corpus = corpus.multiply(BigDecimal.ONE.add(monthlyRate), MC).add(contribution);
            if (corpus.compareTo(targetAmount) >= 0) {
                return month;
            }
        }
        return null; // not reachable within the extended horizon
    }

    private List<MonthlyScheduleItem> buildSchedule(BigDecimal monthlyContribution, BigDecimal monthlyRate, int months) {
        List<MonthlyScheduleItem> schedule = new ArrayList<>();
        BigDecimal cumulative = BigDecimal.ZERO;
        BigDecimal corpus = BigDecimal.ZERO;

        for (int month = 1; month <= months; month++) {
            cumulative = cumulative.add(monthlyContribution);
            corpus = corpus.multiply(BigDecimal.ONE.add(monthlyRate), MC).add(monthlyContribution);

            schedule.add(MonthlyScheduleItem.builder()
                    .monthNumber(month)
                    .plannedSavings(monthlyContribution.setScale(2, RoundingMode.HALF_UP))
                    .cumulativeSavings(cumulative.setScale(2, RoundingMode.HALF_UP))
                    .projectedCorpus(corpus.setScale(2, RoundingMode.HALF_UP))
                    .build());
        }
        return schedule;
    }

    private List<String> investmentProductsFor(String risk, String goal) {
        List<String> products = new ArrayList<>();
        switch (risk) {
            case "CONSERVATIVE":
                products.add("Fixed Deposits (FDs) for capital protection and assured returns");
                products.add("Recurring Deposits (RDs) to build monthly savings discipline");
                products.add("Public Provident Fund (PPF) for long-term, tax-free compounding");
                products.add("Short-duration Debt Mutual Funds for liquidity with modest returns");
                break;
            case "AGGRESSIVE":
                products.add("Equity Mutual Funds (diversified / flexi-cap) for long-term growth");
                products.add("Index Funds tracking broad market benchmarks for low-cost equity exposure");
                products.add("Direct Equity for investors comfortable with market volatility");
                products.add("ELSS funds for tax-saving equity exposure under Section 80C");
                break;
            default:
                products.add("Balanced / Hybrid Mutual Funds blending equity and debt");
                products.add("ELSS funds for tax-efficient, moderate-risk equity exposure");
                products.add("National Pension System (NPS) for long-term retirement-linked goals");
                products.add("Fixed Deposits for a stable portion of the portfolio");
        }
        if (goal != null && goal.toUpperCase().contains("INSURANCE") || (goal != null && goal.toUpperCase().contains("PROTECTION"))) {
            products.add("Term Life Insurance to protect dependents against income loss");
        }
        if (goal != null && goal.toUpperCase().contains("RETIREMENT")) {
            products.add("National Pension System (NPS) for additional retirement corpus and tax benefits under 80CCD(1B)");
        }
        return products;
    }

    private List<String> loanOptionsFor(FinancialPlanRequest request) {
        List<String> options = new ArrayList<>();
        String goal = request.getGoal() == null ? "" : request.getGoal().toUpperCase();

        if (goal.contains("HOME") || goal.contains("HOUSE") || goal.contains("PROPERTY")) {
            String advice = loanAgent.recommendLoanProduct(null, "home purchase");
            options.add(advice);
            if (request.getTargetAmount() != null) {
                EmiCalculationResponse emi = loanAgent.calculateEmi(request.getTargetAmount(),
                        new BigDecimal("8.50"), Math.max(request.getTimelineMonths(), 60));
                options.add(String.format("Indicative Home Loan EMI for INR %s over %d months: approximately INR %s/month.",
                        request.getTargetAmount(), Math.max(request.getTimelineMonths(), 60), emi.getEmi()));
            }
        } else if (goal.contains("EDUCATION")) {
            options.add("An Education Loan could supplement savings for tuition costs, typically offered at " +
                    "concessional rates with a moratorium until course completion.");
        } else if (goal.contains("VEHICLE") || goal.contains("CAR") || goal.contains("AUTO")) {
            options.add(loanAgent.recommendLoanProduct(null, "car purchase"));
        } else {
            options.add("No loan product is typically required for this goal if the recommended savings plan is followed consistently.");
        }
        return options;
    }

    private List<String> regulatoryAndTaxNotesFor(String risk) {
        List<String> notes = new ArrayList<>();
        notes.add("Equity and equity mutual fund long-term capital gains above INR 1,25,000 in a financial year are taxed at 12.5%; short-term gains are taxed at 20%.");
        notes.add("PPF enjoys EEE (Exempt-Exempt-Exempt) tax status: contributions, interest, and maturity proceeds are all tax-free, subject to the annual contribution cap.");
        notes.add("Fixed Deposit interest is fully taxable as per your income slab, and banks deduct TDS if interest exceeds the prescribed threshold in a financial year.");
        notes.add("ELSS investments carry a mandatory 3-year lock-in and qualify for deduction under Section 80C up to the overall limit of INR 1,50,000.");
        if ("AGGRESSIVE".equals(risk)) {
            notes.add("Higher equity allocation increases short-term volatility; ensure this aligns with your stated risk tolerance and investment horizon.");
        }
        notes.add("This plan is indicative and not investment advice; please consult the bank's certified financial advisor before acting on specific recommendations.");
        return notes;
    }

    private String buildNarrative(FinancialPlanRequest request, BigDecimal surplus, BigDecimal recommended,
                                   String risk, boolean achievable, Integer goalMonths) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("With a monthly income of INR %s and expenses of INR %s, your available surplus is INR %s. ",
                request.getMonthlyIncome(), request.getMonthlyExpenses(), surplus.setScale(2, RoundingMode.HALF_UP)));
        sb.append(String.format("We recommend saving/investing INR %s per month toward your goal of \"%s\" using a %s risk approach. ",
                recommended.setScale(2, RoundingMode.HALF_UP), request.getGoal(), risk.toLowerCase()));
        if (request.getTargetAmount() != null) {
            if (achievable) {
                sb.append(String.format("At this rate, your target of INR %s is achievable within your requested timeline of %d months.",
                        request.getTargetAmount(), request.getTimelineMonths()));
            } else if (goalMonths != null) {
                sb.append(String.format("At this rate, reaching your target of INR %s would take approximately %d months, " +
                        "longer than your requested timeline of %d months. Consider increasing your monthly contribution or extending the timeline.",
                        request.getTargetAmount(), goalMonths, request.getTimelineMonths()));
            } else {
                sb.append("At the current surplus, this goal is not comfortably achievable; consider reducing expenses or extending the timeline.");
            }
        }
        return sb.toString();
    }

    @Override
    public AgentResponse handle(AgentRequest request) {
        try {
            String summary = "I can generate a detailed financial plan if you share your monthly income, expenses, " +
                    "any existing EMIs, your goal, target timeline, and risk appetite (conservative, moderate, or " +
                    "aggressive) - or use the Financial Plan Generator on your dashboard for a structured form.";
            return AgentResponse.builder()
                    .agentName(getName())
                    .summary(summary)
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
