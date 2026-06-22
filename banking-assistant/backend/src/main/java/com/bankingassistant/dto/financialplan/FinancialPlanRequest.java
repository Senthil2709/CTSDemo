package com.bankingassistant.dto.financialplan;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FinancialPlanRequest {

    @NotNull
    @DecimalMin(value = "0")
    private BigDecimal monthlyIncome;

    @NotNull
    @DecimalMin(value = "0")
    private BigDecimal monthlyExpenses;

    @DecimalMin(value = "0")
    private BigDecimal existingMonthlyEmi = BigDecimal.ZERO;

    @NotBlank
    private String goal; // e.g. HOME_PURCHASE, RETIREMENT, EDUCATION, EMERGENCY_FUND, OTHER

    @NotNull
    @Min(1)
    private Integer timelineMonths;

    @NotBlank
    private String riskAppetite; // CONSERVATIVE / MODERATE / AGGRESSIVE

    private BigDecimal targetAmount;
}
