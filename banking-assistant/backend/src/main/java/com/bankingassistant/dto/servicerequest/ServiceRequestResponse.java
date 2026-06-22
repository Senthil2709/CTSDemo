package com.bankingassistant.dto.servicerequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequestResponse {
    private UUID id;
    private UUID userId;
    private String requestType;
    private UUID referenceId;
    private String status;
    private String payload;
    private LocalDateTime createdAt;
    private String decisionNotes;
}
