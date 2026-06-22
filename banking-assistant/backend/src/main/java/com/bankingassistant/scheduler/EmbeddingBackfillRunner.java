package com.bankingassistant.scheduler;

import com.bankingassistant.entity.PolicyDocument;
import com.bankingassistant.llm.OpenAiClient;
import com.bankingassistant.rag.PolicyDocumentDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Runs once at application startup. Generates OpenAI embeddings for any
 * seeded (or newly added) policy documents whose "embedding" column is still
 * NULL, so the semantic RAG search in Module 5 has vectors to compare against.
 * No-ops gracefully if app.openai.api-key is not configured.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmbeddingBackfillRunner implements ApplicationRunner {

    private final OpenAiClient openAiClient;
    private final PolicyDocumentDao policyDocumentDao;

    @Override
    public void run(ApplicationArguments args) {
        if (!openAiClient.isConfigured()) {
            log.warn("OPENAI_API_KEY not set - skipping policy embedding backfill. " +
                    "Semantic RAG search will fall back to keyword search until a key is configured.");
            return;
        }

        List<PolicyDocument> pending = policyDocumentDao.findAllWithoutEmbedding();
        if (pending.isEmpty()) {
            log.info("All policy documents already have embeddings.");
            return;
        }

        log.info("Backfilling embeddings for {} policy document(s)...", pending.size());
        for (PolicyDocument doc : pending) {
            float[] embedding = openAiClient.createEmbedding(doc.getTitle() + "\n" + doc.getContent());
            if (embedding != null) {
                policyDocumentDao.updateEmbedding(doc.getId(), embedding);
                log.info("Embedded policy document: {}", doc.getTitle());
            } else {
                log.warn("Failed to embed policy document: {}", doc.getTitle());
            }
        }
    }
}
