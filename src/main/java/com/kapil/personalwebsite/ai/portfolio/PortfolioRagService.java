package com.kapil.personalwebsite.ai.portfolio;

/**
 * Service responsible for building a concise summary of the portfolio context to be used in RAG-first prompting.
 * Aggregates key information from the portfolio and distills it into a summary for vector-retrieved documents.
 *
 * @author Kapil Garg
 */
public interface PortfolioRagService {

    /**
     * Short prompt context so vector-retrieved documents carry most detail (RAG-first prompting).
     */
    String buildPortfolioContextSummary();

}
