package com.kapil.personalwebsite.ai.portfolio;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

/**
 * Implementation of PortfolioChunkRelevanceService to filter and order candidate documents using Jev.
 * Uses a REST API to determine the relevance of each document chunk to a given query.
 *
 * @author Kapil Garg
 */
public class JevPortfolioChunkRelevanceService implements PortfolioChunkRelevanceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JevPortfolioChunkRelevanceService.class);

    private static final String RELEVANCE_QUESTION = "relevance";

    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String apiKey;
    private final String model;
    private final double relevanceThreshold;
    private final boolean logDecisions;
    private final TaskExecutor taskExecutor;

    public JevPortfolioChunkRelevanceService(
            RestTemplate restTemplate,
            String apiUrl,
            String apiKey,
            String model,
            double relevanceThreshold,
            boolean logDecisions,
            @Qualifier("jevTaskExecutor") TaskExecutor taskExecutor) {
        this.restTemplate = restTemplate;
        this.apiUrl = validateApiUrl(apiUrl);
        this.apiKey = apiKey;
        this.model = model;
        if (!Double.isFinite(relevanceThreshold) || relevanceThreshold < 0 || relevanceThreshold > 1) {
            throw new IllegalArgumentException("Jev relevance threshold must be between 0 and 1");
        }
        this.relevanceThreshold = relevanceThreshold;
        this.logDecisions = logDecisions;
        this.taskExecutor = taskExecutor;
    }

    private static String validateApiUrl(String apiUrl) {
        URI uri;
        try {
            uri = URI.create(apiUrl);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Jev API URL is invalid", ex);
        }
        boolean valid = "https".equalsIgnoreCase(uri.getScheme())
                && "api.typesafe.ai".equalsIgnoreCase(uri.getHost())
                && (uri.getPort() == -1 || uri.getPort() == 443)
                && "/v1/systemone".equals(uri.getPath())
                && uri.getUserInfo() == null
                && uri.getQuery() == null
                && uri.getFragment() == null;
        if (!valid) {
            throw new IllegalArgumentException(
                    "Jev API URL must use https://api.typesafe.ai/v1/systemone");
        }
        return uri.toString();
    }

    @Override
    public List<Document> filterRelevantDocuments(String query, List<Document> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        if (apiKey == null || apiKey.isBlank()) {
            LOGGER.warn("Jev relevance filtering is enabled but no API key is configured; preserving vector results");
            return List.copyOf(candidates);
        }
        List<CompletableFuture<ScoredDocument>> decisionTasks = new ArrayList<>(candidates.size());
        IntStream.range(0, candidates.size()).forEach(index -> {
            Document candidate = candidates.get(index);
            decisionTasks.add(CompletableFuture
                    .supplyAsync(() -> decide(query, candidate, index), taskExecutor)
                    .handle((decision, failure) -> failure == null
                            ? decision
                            : failedDecision(candidate, index, failure)));
        });
        List<ScoredDocument> decisions = decisionTasks.stream()
                .map(CompletableFuture::join)
                .toList();
        List<ScoredDocument> retainedDecisions = decisions.stream()
                .filter(decision -> !decision.decisionSucceeded || decision.relevance >= relevanceThreshold)
                .sorted(Comparator.comparing(ScoredDocument::decisionSucceeded, Comparator.reverseOrder())
                        .thenComparing(ScoredDocument::relevance, Comparator.reverseOrder())
                        .thenComparingInt(ScoredDocument::originalIndex))
                .toList();
        logDecisions(decisions, retainedDecisions);
        return retainedDecisions.stream().map(ScoredDocument::document).toList();
    }

    private void logDecisions(List<ScoredDocument> decisions, List<ScoredDocument> retainedDecisions) {
        if (!logDecisions || !LOGGER.isInfoEnabled()) {
            return;
        }
        java.util.Set<Integer> retainedIndexes = retainedDecisions.stream()
                .map(ScoredDocument::originalIndex)
                .collect(java.util.stream.Collectors.toSet());
        String outcomes = decisions.stream()
                .sorted(Comparator.comparingInt(ScoredDocument::originalIndex))
                .map(decision -> decision.decisionSucceeded
                        ? "%d:%.3f:%s".formatted(decision.originalIndex, decision.relevance,
                        retainedIndexes.contains(decision.originalIndex) ? "kept" : "dropped")
                        : "%d:error:kept".formatted(decision.originalIndex))
                .collect(java.util.stream.Collectors.joining(","));
        LOGGER.info("Jev relevance decisions: candidates={}, retained={}, outcomes=[{}]",
                decisions.size(), retainedDecisions.size(), outcomes);
    }

    /**
     * Makes a relevance decision for a single document chunk using the Jev service.
     * If the service fails or returns an invalid response, the document is preserved and marked as unscored.
     *
     * @param query         The query string to evaluate against.
     * @param document      The document chunk to evaluate.
     * @param originalIndex The original index of the document in the candidate list.
     * @return A ScoredDocument containing the document, its relevance score, and whether the decision succeeded.
     */
    private ScoredDocument decide(String query, Document document, int originalIndex) {
        try {
            Map<String, Object> requestBody = getStringObjectMap(query, document);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            ResponseEntity<JevResponse> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers),
                    JevResponse.class
            );
            JevAnswer answer = response.getBody() == null || response.getBody().answers() == null
                    ? null
                    : response.getBody().answers().get(RELEVANCE_QUESTION);
            if (answer == null || answer.noul() == null || answer.noul() < 0 || answer.noul() > 1) {
                LOGGER.warn("Jev returned no valid relevance decision; preserving the vector result");
                return new ScoredDocument(document, 0, originalIndex, false);
            }
            return new ScoredDocument(document, answer.noul(), originalIndex, true);
        } catch (RuntimeException ex) {
            return failedDecision(document, originalIndex, ex);
        }
    }

    private ScoredDocument failedDecision(Document document, int originalIndex, Throwable failure) {
        Throwable rootCause = failure;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        LOGGER.warn("Jev relevance request failed; preserving the vector result ({}: {})",
                rootCause.getClass().getSimpleName(), rootCause.getMessage());
        return new ScoredDocument(document, 0, originalIndex, false);
    }

    private @NonNull Map<String, Object> getStringObjectMap(String query, Document document) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("query", query);
        state.put("chunk", document.getText() == null ? "" : document.getText());
        Map<String, Object> question = Map.of(
                "type", "noul",
                "instructions", "Does this passage contain information that helps answer the query?"
        );
        return Map.of(
                "model", model,
                "state", state,
                "questions", Map.of(RELEVANCE_QUESTION, question)
        );
    }

    private record JevResponse(Map<String, JevAnswer> answers) {
    }

    private record JevAnswer(String type, Double noul) {
    }

    private record ScoredDocument(Document document, double relevance, int originalIndex, boolean decisionSucceeded) {
    }

}
