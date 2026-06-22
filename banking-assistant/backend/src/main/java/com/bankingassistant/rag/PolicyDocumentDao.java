package com.bankingassistant.rag;

import com.bankingassistant.entity.PolicyDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Direct JDBC access to the policy_documents table. We bypass Spring Data JPA
 * here because the embedding column uses the pgvector "vector" type, which we
 * bind/read as a plain SQL literal string (e.g. "[0.01,0.02,...]") cast with
 * "::vector" in the query. This sidesteps the need to register a custom JDBC
 * type per pooled connection.
 */
@Repository
@RequiredArgsConstructor
public class PolicyDocumentDao {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<PolicyDocument> BASE_ROW_MAPPER = (rs, rowNum) -> PolicyDocument.builder()
            .id(UUID.fromString(rs.getString("id")))
            .title(rs.getString("title"))
            .category(rs.getString("category"))
            .content(rs.getString("content"))
            .createdAt(toLdt(rs.getTimestamp("created_at")))
            .updatedAt(toLdt(rs.getTimestamp("updated_at")))
            .build();

    private static LocalDateTime toLdt(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }

    /** Converts a float[] embedding into the pgvector text literal format: [v1,v2,...] */
    public static String toVectorLiteral(float[] embedding) {
        String joined = IntStream.range(0, embedding.length)
                .mapToObj(i -> String.valueOf(embedding[i]))
                .collect(Collectors.joining(","));
        return "[" + joined + "]";
    }

    public List<PolicyDocument> findAllWithoutEmbedding() {
        String sql = "SELECT id, title, category, content, created_at, updated_at " +
                "FROM policy_documents WHERE embedding IS NULL";
        return jdbcTemplate.query(sql, BASE_ROW_MAPPER);
    }

    public void updateEmbedding(UUID id, float[] embedding) {
        String sql = "UPDATE policy_documents SET embedding = CAST(? AS vector), updated_at = now() WHERE id = ?";
        jdbcTemplate.update(sql, toVectorLiteral(embedding), id);
    }

    public PolicyDocument insert(String title, String category, String content, float[] embedding) {
        UUID id = UUID.randomUUID();
        String sql = "INSERT INTO policy_documents (id, title, category, content, embedding) " +
                "VALUES (?, ?, ?, ?, CAST(? AS vector))";
        jdbcTemplate.update(sql, id, title, category, content, toVectorLiteral(embedding));
        return PolicyDocument.builder().id(id).title(title).category(category).content(content).build();
    }

    /**
     * Cosine-similarity search using pgvector's "<=>" cosine-distance operator
     * (smaller distance = more similar). Returns the topK closest documents
     * along with their distance score.
     */
    public List<PolicyDocument> findTopKSimilar(float[] queryEmbedding, int topK) {
        String sql = "SELECT id, title, category, content, created_at, updated_at, " +
                "embedding <=> CAST(? AS vector) AS distance " +
                "FROM policy_documents " +
                "WHERE embedding IS NOT NULL " +
                "ORDER BY distance ASC " +
                "LIMIT ?";

        String literal = toVectorLiteral(queryEmbedding);

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            PolicyDocument doc = BASE_ROW_MAPPER.mapRow(rs, rowNum);
            doc.setDistance(rs.getDouble("distance"));
            return doc;
        }, literal, topK);
    }

    /** Simple keyword fallback used when no embeddings are available yet (e.g. no OpenAI key configured). */
    public List<PolicyDocument> findByKeyword(String keyword, int limit) {
        String sql = "SELECT id, title, category, content, created_at, updated_at FROM policy_documents " +
                "WHERE content ILIKE ? OR title ILIKE ? LIMIT ?";
        String pattern = "%" + keyword + "%";
        return jdbcTemplate.query(sql, BASE_ROW_MAPPER, pattern, pattern, limit);
    }
}
