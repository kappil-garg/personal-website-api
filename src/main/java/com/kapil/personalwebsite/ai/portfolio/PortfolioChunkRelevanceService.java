package com.kapil.personalwebsite.ai.portfolio;

import org.springframework.ai.document.Document;

import java.util.List;

/**
 * Service interface for filtering and ordering candidate documents based on their relevance to a given query.
 *
 * @author Kapil Garg
 */
public interface PortfolioChunkRelevanceService {

    /**
     * Filters and orders the provided list of candidate documents based on their relevance to the given query.
     * Documents that are deemed relevant are returned in order of their relevance score.
     */
    List<Document> filterRelevantDocuments(String query, List<Document> candidates);

}
