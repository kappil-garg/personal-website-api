package com.kapil.personalwebsite.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Chat source information for portfolio chatbot responses, representing the source that informed the answer.
 *
 * @author Kapil Garg
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PortfolioChatSource(
        String type,
        String sourceId,
        String title,
        String slug,
        String snippet
) {
}
