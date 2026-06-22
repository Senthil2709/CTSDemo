package com.bankingassistant.repository;

import com.bankingassistant.entity.ChatSession;
import com.bankingassistant.entity.ChatSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {
    Optional<ChatSession> findFirstByUserIdAndStatusOrderByLastActiveAtDesc(UUID userId, ChatSessionStatus status);
    List<ChatSession> findByStatusAndLastActiveAtBefore(ChatSessionStatus status, LocalDateTime threshold);
}
