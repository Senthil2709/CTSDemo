package com.bankingassistant.scheduler;

import com.bankingassistant.entity.ChatSession;
import com.bankingassistant.entity.ChatSessionStatus;
import com.bankingassistant.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Module 4 - Conversational Memory: automatically expires chat sessions that
 * have had no activity for longer than app.chat.session-inactivity-minutes.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatSessionCleanupScheduler {

    private final ChatSessionRepository chatSessionRepository;

    @Value("${app.chat.session-inactivity-minutes}")
    private int inactivityMinutes;

    @Scheduled(fixedRate = 5 * 60 * 1000) // every 5 minutes
    public void expireInactiveSessions() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(inactivityMinutes);
        List<ChatSession> stale = chatSessionRepository.findByStatusAndLastActiveAtBefore(ChatSessionStatus.ACTIVE, threshold);

        if (stale.isEmpty()) {
            return;
        }

        for (ChatSession session : stale) {
            session.setStatus(ChatSessionStatus.EXPIRED);
            session.setExpiredAt(LocalDateTime.now());
        }
        chatSessionRepository.saveAll(stale);
        log.info("Expired {} inactive chat session(s)", stale.size());
    }
}
