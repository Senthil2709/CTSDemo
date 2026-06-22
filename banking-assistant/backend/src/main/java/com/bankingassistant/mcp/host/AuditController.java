package com.bankingassistant.mcp.host;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditMcpClient client;

    public AuditController(AuditMcpClient client) {
        this.client = client;
    }

    @PostMapping("/ask")
    public Map<String, String> ask(@RequestBody Map<String, String> body) {
        String question = body.getOrDefault("question", "");
        String answer = client.searchAuditLogs(question);
        return Map.of("answer", answer);
    }
}
