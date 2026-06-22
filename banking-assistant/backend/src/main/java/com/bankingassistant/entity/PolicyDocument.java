package com.bankingassistant.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a row in policy_documents. This is intentionally NOT a JPA
 * @Entity because the embedding column uses the pgvector "vector" type,
 * which is handled directly through plain JdbcTemplate SQL (binding/reading
 * the vector as a "[v1,v2,...]" text literal cast with "::vector") in
 * {@link com.bankingassistant.rag.PolicyDocumentDao}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyDocument {
    private UUID id;
    private String title;
    private String category;
    private String content;
    private float[] embedding;
    private Double distance; // populated only on similarity-search results
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
