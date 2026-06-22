package com.bankingassistant.rag;

import com.bankingassistant.dto.policy.PolicyAnswerResponse;
import com.bankingassistant.entity.PolicyDocument;
import com.bankingassistant.llm.OpenAiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implements Module 5 - Banking Policy Knowledge Base (Semantic RAG):
 *   1. Embed the incoming question with the OpenAI embedding API
 *   2. Cosine-similarity search against policy_documents via pgvector
 *   3. Feed the retrieved context + question to the chat model to produce a
 *      grounded, customer-friendly answer
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyRagService {

    private static final int TOP_K = 3;

    private final OpenAiClient openAiClient;
    private final PolicyDocumentDao policyDocumentDao;

    public PolicyAnswerResponse answer(String question) {
        List<PolicyDocument> retrieved = retrieveRelevantDocuments(question);

        String answer = generateAnswer(question, retrieved);

        List<PolicyAnswerResponse.SourceDocument> sources = retrieved.stream()
                .map(doc -> PolicyAnswerResponse.SourceDocument.builder()
                        .title(doc.getTitle())
                        .category(doc.getCategory())
                        .relevanceScore(doc.getDistance() == null ? 0.0 : (1.0 - doc.getDistance()))
                        .build())
                .collect(Collectors.toList());

        return PolicyAnswerResponse.builder()
                .answer(answer)
                .sources(sources)
                .build();
    }

    private List<PolicyDocument> retrieveRelevantDocuments(String question) {
        float[] queryEmbedding = openAiClient.createEmbedding(question);
        if (queryEmbedding != null) {
            return policyDocumentDao.findTopKSimilar(queryEmbedding, TOP_K);
        }
        // Fallback: no embedding API key configured -> keyword search on the first
        // few significant words of the question.
        String firstKeyword = question.replaceAll("[^a-zA-Z ]", " ").trim().split("\\s+")[0];
        return policyDocumentDao.findByKeyword(firstKeyword, TOP_K);
    }

    private String generateAnswer(String question, List<PolicyDocument> docs) {
        if (docs.isEmpty()) {
            return "I couldn't find a specific policy document covering that. Please contact your " +
                    "Relationship Manager or check the latest Terms & Conditions on the bank's website.";
        }

        String context = docs.stream()
                .map(d -> "Title: " + d.getTitle() + "\nContent: " + d.getContent())
                .collect(Collectors.joining("\n\n---\n\n"));

        String systemPrompt = "You are a banking policy assistant. Answer the customer's question ONLY using " +
                "the provided policy context. Be concise, accurate, and customer-friendly. If the context does " +
                "not fully answer the question, say so and suggest contacting a Relationship Manager. Never " +
                "invent numbers, rates, or fees that are not in the context.";
        String userPrompt = "Policy context:\n" + context + "\n\nCustomer question: " + question;

        String llmAnswer = openAiClient.chatComplete(systemPrompt, userPrompt);
        if (llmAnswer != null) {
            return llmAnswer.trim();
        }

        // Deterministic fallback when no LLM is configured: return the most relevant document's content.
        PolicyDocument best = docs.get(0);
        return "Based on our \"" + best.getTitle() + "\" policy: " + best.getContent();
    }
}
