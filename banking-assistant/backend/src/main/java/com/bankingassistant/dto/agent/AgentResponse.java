package com.bankingassistant.dto.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentResponse {
    private String agentName;
    private String summary;
    private Object data;
    private boolean success;
    private String error;
}
