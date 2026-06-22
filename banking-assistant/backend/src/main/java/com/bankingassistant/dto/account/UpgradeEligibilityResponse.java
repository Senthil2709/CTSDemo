package com.bankingassistant.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpgradeEligibilityResponse {
    private String currentTier;
    private String recommendedTier;
    private boolean eligibleForUpgrade;
    private List<String> reasons;
}
