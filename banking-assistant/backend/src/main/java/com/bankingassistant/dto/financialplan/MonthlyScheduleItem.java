package com.bankingassistant.dto.financialplan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyScheduleItem {
    private int monthNumber;
    private BigDecimal plannedSavings;
    private BigDecimal cumulativeSavings;
    private BigDecimal projectedCorpus; // includes estimated investment growth
}
