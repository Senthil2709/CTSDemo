package com.bankingassistant.controller;

import com.bankingassistant.agent.PolicyAgent;
import com.bankingassistant.dto.policy.PolicyAnswerResponse;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/policy")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyAgent policyAgent;

    @PostMapping("/ask")
    public ResponseEntity<PolicyAnswerResponse> ask(@RequestBody AskRequest request) {
        return ResponseEntity.ok(policyAgent.ask(request.getQuestion()));
    }

    @Data
    public static class AskRequest {
        @NotBlank
        private String question;
    }
}
