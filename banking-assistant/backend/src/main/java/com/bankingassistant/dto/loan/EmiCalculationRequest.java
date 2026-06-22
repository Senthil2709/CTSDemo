package com.bankingassistant.dto.loan;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EmiCalculationRequest {

    @NotNull
    @DecimalMin(value = "1000")
    private BigDecimal principalAmount;

    @NotNull
    @DecimalMin(value = "0")
    private BigDecimal annualInterestRate;

    @NotNull
    @Min(1)
    private Integer tenureMonths;
}
