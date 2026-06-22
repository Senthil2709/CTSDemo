package com.bankingassistant.orchestrator;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Module 3 - Agent Orchestration: safety and compliance checks performed
 * before any request is routed to an agent. Keeps the check deterministic
 * and dependency-free so it always runs, even without an LLM configured.
 */
@Component
public class SafetyComplianceChecker {

    private static final List<String> BLOCKED_PATTERNS = List.of(
            "launder", "money laundering", "bypass kyc", "fake id", "fake document",
            "evade tax", "tax evasion", "hack into", "steal", "fraudulent transfer",
            "ignore previous instructions", "ignore your instructions", "disregard your guidelines",
            "reveal your system prompt", "you are now"
    );

    public static class CheckResult {
        public final boolean safe;
        public final String reason;

        public CheckResult(boolean safe, String reason) {
            this.safe = safe;
            this.reason = reason;
        }
    }

    public CheckResult check(String message) {
        if (message == null || message.isBlank()) {
            return new CheckResult(false, "Empty message received.");
        }
        String lower = message.toLowerCase();
        for (String pattern : BLOCKED_PATTERNS) {
            if (lower.contains(pattern)) {
                return new CheckResult(false,
                        "This request appears to involve activity that violates banking compliance policy " +
                                "and cannot be processed. Please contact a Relationship Manager for assistance " +
                                "with legitimate banking needs.");
            }
        }
        return new CheckResult(true, null);
    }
}
