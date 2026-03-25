package com.kapil.personalwebsite.ai.dto;

import java.util.List;

/**
 * DTO for portfolio chatbot response.
 *
 * @author Kapil Garg
 */
public record PortfolioChatResponse(
        String reply,
        List<PortfolioChatSource> sources
) {

    public PortfolioChatResponse(String reply) {
        this(reply, List.of());
    }

}
