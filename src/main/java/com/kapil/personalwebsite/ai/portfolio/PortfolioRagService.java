package com.kapil.personalwebsite.ai.portfolio;

import org.springframework.ai.document.Document;

import java.util.List;

/**
 * Service responsible for building RAG-friendly views of portfolio data.
 * This centralizes portfolio-specific formatting so chat services focus only on prompt construction.
 *
 * @author Kapil Garg
 */
public interface PortfolioRagService {

    /**
     * Builds a concise, human-readable context string summarizing the portfolio.
     * This is intended for direct inclusion in a chat prompt without a vector store.
     *
     * @return a formatted text context describing personal info truncated to a safe maximum length
     */
    String buildPortfolioContext();

    /**
     * Builds a collection of documents representing portfolio entities for use in RAG pipelines.
     *
     * @return a list of documents, one per logical portfolio item
     */
    List<Document> buildPortfolioDocuments();

}
