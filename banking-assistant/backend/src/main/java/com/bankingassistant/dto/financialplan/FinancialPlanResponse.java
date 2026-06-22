package com.bankingassistant.dto.financialplan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialPlanResponse {
    private BigDecimal recommendedMonthlySavings;
    private BigDecimal monthlySurplus;
    private List<String> suggestedInvestmentProducts;
    private List<String> loanOptionsIfApplicable;
    private List<MonthlyScheduleItem> monthlySchedule;
    private Integer estimatedGoalAchievementMonths;
    private boolean goalAchievableInTimeline;
    private List<String> regulatoryAndTaxNotes;
    private String narrativeSummary;
}
