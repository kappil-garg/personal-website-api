package com.kapil.personalwebsite.ai.portfolio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

import java.util.List;

/**
 * Retrieves documents via VectorStore similarity search.
 * If a project ID is provided, it will prioritize documents related to that project plus personal info.
 *
 * @author Kapil Garg
 */
public class PortfolioEmbeddingRetrievalServiceImpl implements PortfolioEmbeddingRetrievalService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PortfolioEmbeddingRetrievalServiceImpl.class);

    private final VectorStore vectorStore;

    public PortfolioEmbeddingRetrievalServiceImpl(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public List<Document> findRelevantDocuments(String query, int topK, String projectId) {
        String trimmedQuery = query != null ? query.trim() : "";
        if (trimmedQuery.isEmpty()) {
            return List.of();
        }
        try {
            SearchRequest.Builder requestBuilder = SearchRequest.builder()
                    .query(trimmedQuery)
                    .topK(topK)
                    .similarityThresholdAll();
            if (projectId != null && !projectId.isBlank()) {
                FilterExpressionBuilder fb = new FilterExpressionBuilder();
                requestBuilder.filterExpression(fb.or(
                        fb.eq("projectId", projectId),
                        fb.eq("type", "personal_info")
                ).build());
            }
            return vectorStore.similaritySearch(requestBuilder.build());
        } catch (Exception ex) {
            LOGGER.warn("Vector store similarity search failed for query; returning empty result set", ex);
            return List.of();
        }
    }

}
