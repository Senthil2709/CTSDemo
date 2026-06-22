package com.bankingassistant.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Enables the @Scheduled chat-session-expiry job in ChatSessionCleanupScheduler. */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
