package com.bankingassistant.dto.loan;

import com.bankingassistant.entity.RequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoanDecisionRequest {
    @NotNull
    private RequestStatus status; // APPROVED / REJECTED / DISBURSED

    private String notes;
}
