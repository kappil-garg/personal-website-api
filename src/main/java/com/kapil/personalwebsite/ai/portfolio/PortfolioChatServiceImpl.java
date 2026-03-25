package com.kapil.personalwebsite.ai.portfolio;

import com.kapil.personalwebsite.ai.dto.PortfolioChatRequest;
import com.kapil.personalwebsite.ai.dto.PortfolioChatResponse;
import com.kapil.personalwebsite.ai.dto.PortfolioChatSource;
import com.kapil.personalwebsite.ai.util.PortfolioAiConstants;
import com.kapil.personalwebsite.ai.vector.PortfolioVectorMetadataKeys;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of PortfolioChatService using Spring AI with portfolio data as context for chat interactions.
 * When embeddings are enabled and available, relevant document chunks are retrieved and appended to the context.
 * When unavailable, falls back to context-only mode gracefully.
 *
 * @author Kapil Garg
 */
@Service
public class PortfolioChatServiceImpl implements PortfolioChatService {

    private static final int RETRIEVAL_TOP_K = 8;
    private static final int SNIPPET_MAX_LENGTH = 240;
    private static final int MAX_RELEVANT_ITEMS_SECTION_LENGTH = 3_500;

    private final PortfolioRagService portfolioRagService;
    private final ObjectProvider<PortfolioEmbeddingRetrievalService> embeddingRetrievalServiceProvider;

    private final ChatClient chatClient;

    @Value("${app.features.embeddings.require-retrieval}")
    private boolean requireRetrieval;

    public PortfolioChatServiceImpl(PortfolioRagService portfolioRagService,
                                    ObjectProvider<PortfolioEmbeddingRetrievalService> embeddingRetrievalServiceProvider,
                                    ChatClient.Builder chatClientBuilder) {
        this.portfolioRagService = portfolioRagService;
        this.embeddingRetrievalServiceProvider = embeddingRetrievalServiceProvider;
        this.chatClient = chatClientBuilder
                .defaultSystem(PortfolioAiConstants.CHAT_SYSTEM_PROMPT)
                .build();
    }

    /**
     * Safely extracts a string value from the document metadata map.
     *
     * @param meta the metadata map from the document, may be null
     * @param key  the key to look up in the metadata map
     * @return the string value associated with the key, or an empty string
     */
    private static String metaString(Map<String, Object> meta, String key) {
        if (meta == null) {
            return "";
        }
        Object v = meta.get(key);
        return v != null ? v.toString() : "";
    }

    @Override
    public PortfolioChatResponse chat(PortfolioChatRequest request) {
        String message = request.message() != null ? request.message().trim() : "";
        String projectId = request.projectId() != null && !request.projectId().isBlank()
                ? request.projectId().trim() : null;
        PortfolioEmbeddingRetrievalService retrieval = embeddingRetrievalServiceProvider.getIfAvailable();
        List<Document> relevantDocs = fetchRelevantDocuments(message, projectId, retrieval);
        String context = buildContext(portfolioRagService.buildPortfolioContextSummary(), relevantDocs);
        String userMessage = PortfolioAiConstants.CHAT_USER_MESSAGE_TEMPLATE.formatted(context, message);
        String reply = chatClient.prompt().user(userMessage).call().content();
        if (reply == null || reply.isBlank()) {
            reply = PortfolioAiConstants.CHAT_FALLBACK_REPLY;
        } else {
            reply = reply.trim();
        }
        List<PortfolioChatSource> sources = deduplicateSources(relevantDocs);
        return new PortfolioChatResponse(reply, sources);
    }

    /**
     * Fetches semantically relevant documents for the query, or an empty list when retrieval is unavailable.
     *
     * @param message   the user query/message to find relevant documents for
     * @param projectId optional project ID to scope the retrieval, or null for global
     * @param retrieval the embedding retrieval service to use, or null if unavailable
     * @return a list of relevant Document objects, or an empty list if retrieval is unavailable or returns no results
     */
    private List<Document> fetchRelevantDocuments(String message, String projectId,
                                                  PortfolioEmbeddingRetrievalService retrieval) {
        if (retrieval == null) {
            if (requireRetrieval) {
                throw new IllegalStateException(
                        "Embedding retrieval service is not available. " +
                                "Set app.features.embeddings.enabled=true and configure a Google GenAI API key.");
            }
            return List.of();
        }
        List<Document> docs = retrieval.findRelevantDocuments(message, RETRIEVAL_TOP_K, projectId);
        if (docs.isEmpty() && requireRetrieval) {
            throw new IllegalStateException(
                    "RAG retrieval returned 0 relevant documents (query='" + message + "', projectId=" + projectId + ").");
        }
        return docs;
    }

    /**
     * Builds the full prompt context: portfolio summary + semantically relevant chunks.
     *
     * @param summary      the base portfolio context summary to use in the prompt
     * @param relevantDocs the list of semantically relevant documents to include in the context
     * @return a combined context string for the AI prompt, with a section for relevant items if any are present
     */
    private String buildContext(String summary, List<Document> relevantDocs) {
        if (relevantDocs.isEmpty()) {
            return summary;
        }
        StringBuilder sb = new StringBuilder(summary);
        sb.append(PortfolioAiConstants.CHAT_RELEVANT_ITEMS_HEADER);
        int used = 0;
        for (Document doc : relevantDocs) {
            String line = "- " + doc.getFormattedContent() + "\n";
            if (used + line.length() > MAX_RELEVANT_ITEMS_SECTION_LENGTH) {
                int remaining = MAX_RELEVANT_ITEMS_SECTION_LENGTH - used;
                if (remaining > 0) {
                    sb.append(line, 0, remaining);
                }
                break;
            }
            sb.append(line);
            used += line.length();
        }
        return sb.toString();
    }

    /**
     * Maps retrieved documents to deduplicated chat sources (one entry per unique source slug/id).
     *
     * @param docs the list of retrieved documents to convert into chat sources
     * @return a list of unique PortfolioChatSource objects derived from the documents
     */
    private List<PortfolioChatSource> deduplicateSources(List<Document> docs) {
        if (docs.isEmpty()) {
            return List.of();
        }
        return docs.stream()
                .map(this::toChatSource)
                .collect(LinkedHashMap<String, PortfolioChatSource>::new,
                        (map, s) -> map.putIfAbsent(s.slug() != null ? s.slug() : s.sourceId(), s),
                        LinkedHashMap::putAll)
                .values().stream().toList();
    }

    /**
     * Converts a retrieved Document into a PortfolioChatSource, extracting metadata and creating a text snippet.
     *
     * @param doc the retrieved document to convert into a chat source
     * @return a PortfolioChatSource object
     */
    private PortfolioChatSource toChatSource(Document doc) {
        Map<String, Object> meta = doc.getMetadata();
        String text = doc.getText();
        String snippet = text == null ? "" : text.strip();
        if (snippet.length() > SNIPPET_MAX_LENGTH) {
            snippet = snippet.substring(0, SNIPPET_MAX_LENGTH) + "...";
        }
        String slug = metaString(meta, PortfolioVectorMetadataKeys.SLUG);
        return new PortfolioChatSource(
                metaString(meta, PortfolioVectorMetadataKeys.TYPE),
                metaString(meta, PortfolioVectorMetadataKeys.SOURCE_ID),
                metaString(meta, PortfolioVectorMetadataKeys.TITLE),
                slug.isEmpty() ? null : slug,
                snippet.isEmpty() ? null : snippet
        );
    }

}
