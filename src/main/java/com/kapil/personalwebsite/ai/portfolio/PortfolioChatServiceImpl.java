package com.kapil.personalwebsite.ai.portfolio;

import com.kapil.personalwebsite.ai.dto.PortfolioChatRequest;
import com.kapil.personalwebsite.ai.dto.PortfolioChatResponse;
import com.kapil.personalwebsite.ai.util.PortfolioAiConstants;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * Implementation of PortfolioChatService using Spring AI with portfolio data as context for chat interactions.
 * This service constructs a prompt that includes a system instruction and relevant portfolio information,
 *
 * @author Kapil Garg
 */
@Service
public class PortfolioChatServiceImpl implements PortfolioChatService {

    private final PortfolioRagService portfolioRagService;
    private final PortfolioEmbeddingRetrievalService embeddingRetrievalService;
    private final ChatClient chatClient;

    public PortfolioChatServiceImpl(PortfolioRagService portfolioRagService,
                                    ObjectProvider<PortfolioEmbeddingRetrievalService> embeddingRetrievalServiceProvider,
                                    ChatClient.Builder chatClientBuilder) {
        this.portfolioRagService = portfolioRagService;
        this.embeddingRetrievalService = embeddingRetrievalServiceProvider.getIfAvailable();
        this.chatClient = chatClientBuilder
                .defaultSystem(PortfolioAiConstants.CHAT_SYSTEM_PROMPT)
                .build();
    }

    @Override
    public PortfolioChatResponse chat(PortfolioChatRequest request) {
        String message = request.message() != null ? request.message().trim() : "";
        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append(portfolioRagService.buildPortfolioContext());
        if (embeddingRetrievalService != null) {
            var relevantDocuments = embeddingRetrievalService.findRelevantDocuments(message, 5);
            if (!relevantDocuments.isEmpty()) {
                contextBuilder.append(PortfolioAiConstants.CHAT_RELEVANT_ITEMS_HEADER);
                for (Document doc : relevantDocuments) {
                    contextBuilder.append("- ").append(doc.getFormattedContent()).append("\n");
                }
            }
        }
        String context = contextBuilder.toString();
        String userMessage = PortfolioAiConstants.CHAT_USER_MESSAGE_TEMPLATE.formatted(context, message);
        String reply = chatClient
                .prompt()
                .user(userMessage)
                .call()
                .content();
        if (reply == null || reply.isBlank()) {
            reply = PortfolioAiConstants.CHAT_FALLBACK_REPLY;
        } else {
            reply = reply.trim();
        }
        return new PortfolioChatResponse(reply);
    }

}
