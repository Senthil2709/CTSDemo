package com.bankingassistant.mcp.server;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditMcpServerConfig {

    /**
     * Registers AuditTools methods as MCP tool callbacks so the MCP server
     * can advertise and dispatch them over stdio.
     */
    @Bean
    public ToolCallbackProvider auditToolCallbackProvider(AuditTools auditTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(auditTools)
                .build();
    }
}
