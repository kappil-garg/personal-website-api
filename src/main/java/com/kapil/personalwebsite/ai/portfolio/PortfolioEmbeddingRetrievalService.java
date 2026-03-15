package com.kapil.personalwebsite.ai.portfolio;

import org.springframework.ai.document.Document;

import java.util.List;

/**
 * Service that uses embeddings to retrieve the most relevant portfolio documents for a given visitor question.
 * This enables semantic retrieval without having to manually craft filters per entity type.
 *
 * @author Kapil Garg
 */
public interface PortfolioEmbeddingRetrievalService {

    /**
     * Finds the most relevant portfolio documents for the given query using embeddings-based similarity.
     *
     * @param query the visitor's question
     * @param topK  maximum number of documents to return
     * @return a list of relevant documents sorted by descending similarity
     */
    List<Document> findRelevantDocuments(String query, int topK);

}
