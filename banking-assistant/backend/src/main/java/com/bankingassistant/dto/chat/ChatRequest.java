package com.bankingassistant.dto.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class ChatRequest {
    @NotBlank
    private String message;

    /** Optional - if absent / expired, the orchestrator creates a fresh session. */
    private UUID sessionId;
}
