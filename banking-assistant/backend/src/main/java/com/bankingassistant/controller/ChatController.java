package com.bankingassistant.controller;

import com.bankingassistant.dto.chat.ChatHistoryResponse;
import com.bankingassistant.dto.chat.ChatRequest;
import com.bankingassistant.dto.chat.ChatResponse;
import com.bankingassistant.orchestrator.OrchestratorService;
import com.bankingassistant.security.SecurityUser;
import com.bankingassistant.service.ChatMemoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final OrchestratorService orchestratorService;
    private final ChatMemoryService chatMemoryService;

    @PostMapping("/message")
    public ResponseEntity<ChatResponse> sendMessage(@AuthenticationPrincipal SecurityUser principal,
                                                      @Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(orchestratorService.handle(principal.getId(), request));
    }

    @GetMapping("/{sessionId}/history")
    public ResponseEntity<ChatHistoryResponse> history(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(chatMemoryService.retrieveHistory(sessionId));
    }
}
