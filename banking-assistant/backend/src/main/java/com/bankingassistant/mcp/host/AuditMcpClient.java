package com.bankingassistant.mcp.host;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "spring.ai.mcp.client.enabled", havingValue = "true")
public class AuditMcpClient {

    private final ChatClient chatClient;

    public AuditMcpClient(ChatClient.Builder builder,
                           ToolCallbackProvider toolCallbackProvider) {
        this.chatClient = builder
                .defaultToolCallbacks(toolCallbackProvider.getToolCallbacks())
                .build();
    }

    public String searchAuditLogs(String prompt) {
        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}
