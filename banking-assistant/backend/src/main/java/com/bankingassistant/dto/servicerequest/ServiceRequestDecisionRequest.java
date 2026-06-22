package com.bankingassistant.dto.servicerequest;

import com.bankingassistant.entity.RequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ServiceRequestDecisionRequest {
    @NotNull
    private RequestStatus status;
    private String notes;
}
