package com.bankingassistant.orchestrator;

import com.bankingassistant.agent.AccountAgent;
import com.bankingassistant.agent.FinancialPlanningAgent;
import com.bankingassistant.agent.LoanAgent;
import com.bankingassistant.agent.PolicyAgent;
import com.bankingassistant.dto.agent.AgentRequest;
import com.bankingassistant.dto.agent.AgentResponse;
import com.bankingassistant.dto.agent.Intent;
import com.bankingassistant.dto.chat.ChatRequest;
import com.bankingassistant.dto.chat.ChatResponse;
import com.bankingassistant.entity.ChatSession;
import com.bankingassistant.entity.MessageSender;
import com.bankingassistant.llm.OpenAiClient;
import com.bankingassistant.service.ChatMemoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * Module 3 - Agent Orchestration.
 * Receives customer requests, performs safety/compliance checks, classifies
 * intent, routes to the appropriate agent(s), executes multiple agents in
 * parallel when required (e.g. financial planning combines Account + Loan +
 * Financial Planning agents), and merges responses into a single
 * customer-friendly answer.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrchestratorService {

    private final SafetyComplianceChecker safetyComplianceChecker;
    private final ChatMemoryService chatMemoryService;
    private final OpenAiClient openAiClient;

    private final AccountAgent accountAgent;
    private final LoanAgent loanAgent;
    private final PolicyAgent policyAgent;
    private final FinancialPlanningAgent financialPlanningAgent;

    @Qualifier("agentExecutor")
    private final Executor agentExecutor;

    public ChatResponse handle(UUID userId, ChatRequest request) {
        // 1. Safety / compliance check
        SafetyComplianceChecker.CheckResult check = safetyComplianceChecker.check(request.getMessage());

        ChatSession session = chatMemoryService.createOrGetSession(userId, request.getSessionId());

        if (!check.safe) {
            chatMemoryService.saveMessage(session.getId(), MessageSender.USER, "BLOCKED", request.getMessage());
            chatMemoryService.saveMessage(session.getId(), MessageSender.ASSISTANT, "BLOCKED", check.reason);
            return ChatResponse.builder()
                    .sessionId(session.getId())
                    .reply(check.reason)
                    .intentsHandled(List.of())
                    .blocked(true)
                    .build();
        }

        chatMemoryService.saveMessage(session.getId(), MessageSender.USER, null, request.getMessage());

        // 2. Intent classification
        Intent intent = classifyIntent(request.getMessage());

        // 3. Route to agent(s), running in parallel for compound requests
        List<AgentResponse> agentResponses = route(userId, request.getMessage(), intent);

        // 4. Merge responses into a single customer-friendly reply
        String reply = mergeResponses(request.getMessage(), agentResponses);

        List<String> intentsHandled = agentResponses.stream().map(AgentResponse::getAgentName).collect(Collectors.toList());

        chatMemoryService.saveMessage(session.getId(), MessageSender.ASSISTANT, intent.name(), reply);

        return ChatResponse.builder()
                .sessionId(session.getId())
                .reply(reply)
                .intentsHandled(intentsHandled)
                .blocked(false)
                .build();
    }

    private Intent classifyIntent(String message) {
        if (openAiClient.isConfigured()) {
            String systemPrompt = "Classify the banking customer's message into exactly one of these intents: " +
                    "ACCOUNT_INQUIRY, LOAN_INQUIRY, POLICY_QUESTION, FINANCIAL_PLANNING, GENERAL. " +
                    "Respond with ONLY the intent label, nothing else.";
            String result = openAiClient.chatComplete(systemPrompt, message);
            if (result != null) {
                String cleaned = result.trim().toUpperCase().replaceAll("[^A-Z_]", "");
                for (Intent candidate : Intent.values()) {
                    if (candidate.name().equals(cleaned)) {
                        return candidate;
                    }
                }
            }
        }
        return classifyIntentByKeyword(message);
    }

    private Intent classifyIntentByKeyword(String message) {
        String lower = message.toLowerCase();

        boolean mentionsAccount = lower.contains("balance") || lower.contains("transaction") ||
                lower.contains("account") || lower.contains("spending") || lower.contains("upgrade");
        boolean mentionsLoan = lower.contains("loan") || lower.contains("emi") || lower.contains("borrow") ||
                lower.contains("credit");
        boolean mentionsPolicy = lower.contains("policy") || lower.contains("kyc") || lower.contains("fee") ||
                lower.contains("interest rate") || lower.contains("terms") || lower.contains("regulation") ||
                lower.contains("grievance") || lower.contains("otp");
        boolean mentionsPlanning = lower.contains("plan") || lower.contains("invest") || lower.contains("save") ||
                lower.contains("savings") || lower.contains("retirement") || lower.contains("goal") ||
                lower.contains("budget");

        // Financial planning requests inherently touch accounts and loans too,
        // so prioritise it when the message clearly signals a planning intent.
        if (mentionsPlanning) {
            return Intent.FINANCIAL_PLANNING;
        }
        if (mentionsLoan) {
            return Intent.LOAN_INQUIRY;
        }
        if (mentionsPolicy) {
            return Intent.POLICY_QUESTION;
        }
        if (mentionsAccount) {
            return Intent.ACCOUNT_INQUIRY;
        }
        return Intent.GENERAL;
    }

    private List<AgentResponse> route(UUID userId, String message, Intent intent) {
        AgentRequest agentRequest = AgentRequest.builder().userId(userId).message(message).intent(intent).build();

        if (intent == Intent.FINANCIAL_PLANNING) {
            // Per Module 3's example: Account, Loan, and Financial Planning agents
            // execute IN PARALLEL for compound financial-planning requests.
            CompletableFuture<AgentResponse> accountFuture =
                    CompletableFuture.supplyAsync(() -> accountAgent.handle(agentRequest), agentExecutor);
            CompletableFuture<AgentResponse> loanFuture =
                    CompletableFuture.supplyAsync(() -> loanAgent.handle(agentRequest), agentExecutor);
            CompletableFuture<AgentResponse> planningFuture =
                    CompletableFuture.supplyAsync(() -> financialPlanningAgent.handle(agentRequest), agentExecutor);

            CompletableFuture.allOf(accountFuture, loanFuture, planningFuture).join();

            List<AgentResponse> responses = new ArrayList<>();
            responses.add(accountFuture.join());
            responses.add(loanFuture.join());
            responses.add(planningFuture.join());
            return responses;
        }

        AgentResponse single = switch (intent) {
            case ACCOUNT_INQUIRY -> accountAgent.handle(agentRequest);
            case LOAN_INQUIRY -> loanAgent.handle(agentRequest);
            case POLICY_QUESTION -> policyAgent.handle(agentRequest);
            default -> AgentResponse.builder()
                    .agentName("GeneralAssistant")
                    .summary("I can help with account balances and history, loan eligibility and EMI " +
                            "calculations, banking policy questions, and personalised financial planning. " +
                            "What would you like help with today?")
                    .success(true)
                    .build();
        };
        return List.of(single);
    }

    private String mergeResponses(String originalMessage, List<AgentResponse> responses) {
        List<AgentResponse> successful = responses.stream().filter(AgentResponse::isSuccess).collect(Collectors.toList());

        if (successful.isEmpty()) {
            return "I'm sorry, I wasn't able to process that request right now. Please try again shortly or " +
                    "contact your Relationship Manager.";
        }

        if (successful.size() == 1) {
            return successful.get(0).getSummary();
        }

        // Multiple agents contributed (e.g. financial planning) - merge their
        // individual summaries into one coherent, customer-friendly answer.
        if (openAiClient.isConfigured()) {
            String combinedContext = successful.stream()
                    .map(r -> r.getAgentName() + ": " + r.getSummary())
                    .collect(Collectors.joining("\n\n"));
            String systemPrompt = "You are a helpful banking assistant. Combine the following specialist agent " +
                    "outputs into a single, coherent, customer-friendly response. Do not mention the agent names. " +
                    "Be concise and avoid inventing any numbers not present in the input.";
            String merged = openAiClient.chatComplete(systemPrompt,
                    "Customer asked: " + originalMessage + "\n\nAgent outputs:\n" + combinedContext);
            if (merged != null) {
                return merged.trim();
            }
        }

        // Deterministic fallback: concatenate each agent's summary.
        return successful.stream().map(AgentResponse::getSummary).collect(Collectors.joining("\n\n"));
    }
}
