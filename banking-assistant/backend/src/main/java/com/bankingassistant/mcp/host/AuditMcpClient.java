package com.bankingassistant.mcp.host;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;

@Service
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
