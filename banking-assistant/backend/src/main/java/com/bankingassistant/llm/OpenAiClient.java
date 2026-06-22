package com.bankingassistant.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Thin wrapper around the OpenAI REST API used for:
 *  - chat completions (intent classification, response generation, narrative
 *    generation for financial plans, merging multi-agent responses)
 *  - text embeddings (semantic policy RAG)
 *
 * If app.openai.api-key is not configured, callers fall back to deterministic,
 * rule-based behaviour so the rest of the application keeps working without
 * an API key (useful for local development / demos).
 */
@Component
@Slf4j
public class OpenAiClient {

    private final RestClient restClient;
    private final String apiKey;
    private final String chatModel;
    private final String embeddingModel;
    private final int embeddingDimensions;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAiClient(RestClient.Builder builder,
                         @Value("${app.openai.api-key}") String apiKey,
                         @Value("${app.openai.base-url}") String baseUrl,
                         @Value("${app.openai.chat-model}") String chatModel,
                         @Value("${app.openai.embedding-model}") String embeddingModel,
                         @Value("${app.openai.embedding-dimensions}") int embeddingDimensions) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
        this.chatModel = chatModel;
        this.embeddingModel = embeddingModel;
        this.embeddingDimensions = embeddingDimensions;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public int getEmbeddingDimensions() {
        return embeddingDimensions;
    }

    /**
     * Calls POST /chat/completions with a system + user message and returns the
     * assistant's text content. Returns null if no API key is configured or the
     * call fails, so callers can apply a deterministic fallback.
     */
    public String chatComplete(String systemPrompt, String userPrompt) {
        if (!isConfigured()) {
            return null;
        }
        try {
            Map<String, Object> body = Map.of(
                    "model", chatModel,
                    "temperature", 0.3,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
                    )
            );

            String raw = restClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(raw);
            return root.path("choices").path(0).path("message").path("content").asText(null);
        } catch (Exception ex) {
            log.warn("OpenAI chat completion failed, falling back to rule-based response: {}", ex.getMessage());
            return null;
        }
    }

    /**
     * Calls POST /embeddings for the given text and returns the embedding vector.
     * Returns null if no API key is configured or the call fails.
     */
    public float[] createEmbedding(String text) {
        if (!isConfigured()) {
            return null;
        }
        try {
            Map<String, Object> body = Map.of(
                    "model", embeddingModel,
                    "input", text
            );

            String raw = restClient.post()
                    .uri("/embeddings")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(raw);
            ArrayNode embeddingNode = (ArrayNode) root.path("data").path(0).path("embedding");
            float[] vector = new float[embeddingNode.size()];
            for (int i = 0; i < embeddingNode.size(); i++) {
                vector[i] = embeddingNode.get(i).floatValue();
            }
            return vector;
        } catch (Exception ex) {
            log.warn("OpenAI embedding call failed: {}", ex.getMessage());
            return null;
        }
    }
}
