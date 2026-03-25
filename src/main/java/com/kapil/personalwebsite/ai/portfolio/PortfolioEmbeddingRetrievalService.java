package com.kapil.personalwebsite.ai.portfolio;

import org.springframework.ai.document.Document;

import java.util.List;

/**
 * Service for retrieving relevant portfolio documents based on a query.
 *
 * @author Kapil Garg
 */
public interface PortfolioEmbeddingRetrievalService {

    /**
     * Finds the most relevant documents for the given query.
     * If a project ID is provided, it will prioritize documents related to that project plus personal info.
     *
     * @param query     the query to search for
     * @param topK      the number of documents to return
     * @param projectId the project ID to prioritize, if provided
     * @return the list of relevant documents
     */
    List<Document> findRelevantDocuments(String query, int topK, String projectId);

}
