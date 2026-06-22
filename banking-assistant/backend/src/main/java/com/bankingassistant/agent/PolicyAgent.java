package com.bankingassistant.agent;

import com.bankingassistant.dto.agent.AgentRequest;
import com.bankingassistant.dto.agent.AgentResponse;
import com.bankingassistant.dto.policy.PolicyAnswerResponse;
import com.bankingassistant.rag.PolicyRagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Module 2 - Policy Agent.
 * Answers banking policy / regulatory questions, retrieves relevant policy
 * documents semantically, and explains terms, rates, and fee structures.
 * Delegates the actual semantic RAG pipeline (Module 5) to PolicyRagService.
 */
@Component
@RequiredArgsConstructor
public class PolicyAgent implements Agent {

    private final PolicyRagService policyRagService;

    @Override
    public String getName() {
        return "PolicyAgent";
    }

    public PolicyAnswerResponse ask(String question) {
        return policyRagService.answer(question);
    }

    @Override
    public AgentResponse handle(AgentRequest request) {
        try {
            PolicyAnswerResponse response = ask(request.getMessage());
            return AgentResponse.builder()
                    .agentName(getName())
                    .summary(response.getAnswer())
                    .data(response.getSources())
                    .success(true)
                    .build();
        } catch (Exception ex) {
            return AgentResponse.builder()
                    .agentName(getName())
                    .success(false)
                    .error(ex.getMessage())
                    .build();
        }
    }
}
