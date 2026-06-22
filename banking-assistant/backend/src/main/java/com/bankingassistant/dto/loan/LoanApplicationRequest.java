package com.bankingassistant.dto.loan;

import com.bankingassistant.entity.LoanType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanApplicationRequest {

    @NotNull
    private LoanType loanType;

    @NotNull
    @DecimalMin(value = "1000", message = "Loan amount must be at least 1000")
    private BigDecimal principalAmount;

    @NotNull
    @Min(value = 3, message = "Tenure must be at least 3 months")
    private Integer tenureMonths;

    private String purpose;
}
