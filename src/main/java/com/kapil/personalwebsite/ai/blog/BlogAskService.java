package com.kapil.personalwebsite.ai.blog;

import com.kapil.personalwebsite.ai.dto.BlogAskRequest;
import com.kapil.personalwebsite.ai.dto.BlogAskResponse;

import java.util.Optional;

/**
 * Service for answering questions about a blog post using AI (RAG over post content).
 *
 * @author Kapil Garg
 */
public interface BlogAskService {

    /**
     * Answers a question about the blog post identified by the given slug.
     * Uses the published blog content as context (simple RAG without vector store).
     *
     * @param slug    the slug of the published blog post
     * @param request the request containing the user's question
     * @return an Optional containing the answer if the blog exists and AI responded, empty otherwise
     */
    Optional<BlogAskResponse> ask(String slug, BlogAskRequest request);

}
