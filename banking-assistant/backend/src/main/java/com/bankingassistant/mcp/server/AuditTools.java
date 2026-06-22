package com.bankingassistant.mcp.server;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Component
public class AuditTools {

    private final AuditLogService service;

    public AuditTools(AuditLogService service) {
        this.service = service;
    }

    @PostConstruct
    public void init() {
        System.err.println("AuditTools loaded");
    }

    @Tool(
        name = "searchAuditLogs",
        description = "Search bank audit logs by keyword (action, user, status)"
    )
    public String searchAuditLogs(String keyword) throws Exception {
        List<String> results = service.search(keyword);
        return String.join("\n", results);
    }

    @Tool(
        name = "getTransactionAudit",
        description = "Find audit details for a specific transaction ID"
    )
    public String getTransactionAudit(String transactionId) throws Exception {
        List<String> results = service.search(transactionId);
        return String.join("\n", results);
    }
}
