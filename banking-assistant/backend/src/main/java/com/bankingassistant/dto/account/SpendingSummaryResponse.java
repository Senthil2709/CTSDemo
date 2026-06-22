package com.bankingassistant.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpendingSummaryResponse {
    private BigDecimal totalCredits;
    private BigDecimal totalDebits;
    private Map<String, BigDecimal> spendingByCategory;
    private int periodDays;
}
