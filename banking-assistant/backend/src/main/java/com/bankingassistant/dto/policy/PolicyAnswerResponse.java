package com.bankingassistant.dto.policy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyAnswerResponse {
    private String answer;
    private List<SourceDocument> sources;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SourceDocument {
        private String title;
        private String category;
        private double relevanceScore;
    }
}
