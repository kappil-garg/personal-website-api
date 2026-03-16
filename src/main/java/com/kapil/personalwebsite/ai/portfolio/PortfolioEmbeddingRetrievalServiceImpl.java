package com.kapil.personalwebsite.ai.portfolio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of PortfolioEmbeddingRetrievalService that uses Spring AI to compute embeddings.
 * This service retrieves portfolio documents relevant to a query by comparing their embeddings to the query embedding.
 *
 * @author Kapil Garg
 */
public class PortfolioEmbeddingRetrievalServiceImpl implements PortfolioEmbeddingRetrievalService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PortfolioEmbeddingRetrievalServiceImpl.class);

    private final EmbeddingModel embeddingModel;
    private final PortfolioRagService portfolioRagService;
    private final Map<String, float[]> documentEmbeddingCache = new ConcurrentHashMap<>();

    public PortfolioEmbeddingRetrievalServiceImpl(EmbeddingModel embeddingModel,
                                                  PortfolioRagService portfolioRagService) {
        this.embeddingModel = embeddingModel;
        this.portfolioRagService = portfolioRagService;
    }

    @Override
    public List<Document> findRelevantDocuments(String query, int topK) {
        String trimmedQuery = query != null ? query.trim() : "";
        if (trimmedQuery.isEmpty()) {
            return portfolioRagService.buildPortfolioDocuments();
        }
        try {
            float[] queryEmbedding = embeddingModel.embed(trimmedQuery);
            List<Document> documents = portfolioRagService.buildPortfolioDocuments();
            return documents.stream()
                    .map(doc -> scoreDocument(doc, queryEmbedding))
                    .sorted(Comparator.comparingDouble(ScoredDocument::score).reversed())
                    .limit(topK)
                    .map(ScoredDocument::document)
                    .toList();
        } catch (Exception ex) {
            LOGGER.warn("Failed to retrieve portfolio documents using embeddings. Falling back to full context.", ex);
            return portfolioRagService.buildPortfolioDocuments();
        }
    }

    /**
     * Scores a document by computing the cosine similarity between its embedding and the query embedding.
     *
     * @param document       the document to score
     * @param queryEmbedding the embedding vector of the query
     * @return a ScoredDocument containing the original document and its similarity score
     */
    private ScoredDocument scoreDocument(Document document, float[] queryEmbedding) {
        String content = document.getText();
        String cacheKey = document.getId();
        float[] docEmbedding = documentEmbeddingCache.computeIfAbsent(cacheKey,
                key -> embeddingModel.embed(content != null ? content : ""));
        return new ScoredDocument(document, cosineSimilarity(queryEmbedding, docEmbedding));
    }

    /**
     * Computes cosine similarity between two embedding vectors.
     *
     * @param a the first embedding vector
     * @param b the second embedding vector
     * @return the cosine similarity score, or 0.0 if inputs are invalid
     */
    private double cosineSimilarity(float[] a, float[] b) {
        if (a == null || b == null || a.length == 0 || b.length == 0 || a.length != b.length) {
            return 0.0;
        }
        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private record ScoredDocument(Document document, double score) {
    }

}
