package com.kapil.personalwebsite.ai.portfolio;

import com.kapil.personalwebsite.ai.dto.PortfolioChatRequest;
import com.kapil.personalwebsite.ai.dto.PortfolioChatResponse;

/**
 * Service for answering questions about portfolio (experience, skills, projects, education) using AI.
 *
 * @author Kapil Garg
 */
public interface PortfolioChatService {

    /**
     * Answers a visitor's question about Kapil using portfolio data as context.
     *
     * @param request the chat request containing the visitor's message
     * @return the AI-generated reply
     */
    PortfolioChatResponse chat(PortfolioChatRequest request);

}
