package com.bankingassistant.agent;

import com.bankingassistant.dto.agent.AgentRequest;
import com.bankingassistant.dto.agent.AgentResponse;

/**
 * Common contract implemented by every specialized AI agent (Module 2) so the
 * OrchestratorService can invoke them polymorphically - including running
 * several agents in parallel for compound requests such as financial planning.
 */
public interface Agent {
    String getName();
    AgentResponse handle(AgentRequest request);
}
