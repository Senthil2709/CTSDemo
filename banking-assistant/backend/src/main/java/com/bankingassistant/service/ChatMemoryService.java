package com.bankingassistant.service;

import com.bankingassistant.dto.chat.ChatHistoryResponse;
import com.bankingassistant.dto.chat.ChatMessageDto;
import com.bankingassistant.entity.ChatMessage;
import com.bankingassistant.entity.ChatSession;
import com.bankingassistant.entity.ChatSessionStatus;
import com.bankingassistant.entity.MessageSender;
import com.bankingassistant.repository.ChatMessageRepository;
import com.bankingassistant.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Module 4 - Conversational Memory.
 * Stores conversation history per customer, maintains chat sessions, and
 * works alongside ChatSessionCleanupScheduler to automatically expire
 * inactive sessions.
 */
@Service
@RequiredArgsConstructor
public class ChatMemoryService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;

    /**
     * Reuses the customer's most recent ACTIVE session if one exists and a
     * specific session wasn't requested (or the requested one is no longer
     * active); otherwise starts a brand-new session.
     */
    @Transactional
    public ChatSession createOrGetSession(UUID userId, UUID requestedSessionId) {
        if (requestedSessionId != null) {
            ChatSession existing = chatSessionRepository.findById(requestedSessionId).orElse(null);
            if (existing != null && existing.getUserId().equals(userId)
                    && existing.getStatus() == ChatSessionStatus.ACTIVE) {
                existing.setLastActiveAt(LocalDateTime.now());
                return chatSessionRepository.save(existing);
            }
        }

        return chatSessionRepository.findFirstByUserIdAndStatusOrderByLastActiveAtDesc(userId, ChatSessionStatus.ACTIVE)
                .map(session -> {
                    session.setLastActiveAt(LocalDateTime.now());
                    return chatSessionRepository.save(session);
                })
                .orElseGet(() -> {
                    LocalDateTime now = LocalDateTime.now();
                    ChatSession newSession = ChatSession.builder()
                            .userId(userId)
                            .status(ChatSessionStatus.ACTIVE)
                            .startedAt(now)
                            .lastActiveAt(now)
                            .build();
                    return chatSessionRepository.save(newSession);
                });
    }

    @Transactional
    public ChatMessage saveMessage(UUID sessionId, MessageSender sender, String intent, String content) {
        ChatMessage message = ChatMessage.builder()
                .sessionId(sessionId)
                .sender(sender)
                .intent(intent)
                .content(content)
                .build();
        return chatMessageRepository.save(message);
    }

    public ChatHistoryResponse retrieveHistory(UUID sessionId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Chat session not found"));

        List<ChatMessageDto> messages = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId).stream()
                .map(m -> ChatMessageDto.builder()
                        .sender(m.getSender().name())
                        .intent(m.getIntent())
                        .content(m.getContent())
                        .createdAt(m.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return ChatHistoryResponse.builder()
                .sessionId(session.getId())
                .status(session.getStatus().name())
                .messages(messages)
                .build();
    }
}
