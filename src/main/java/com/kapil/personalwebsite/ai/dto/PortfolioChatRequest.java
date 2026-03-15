package com.kapil.personalwebsite.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for portfolio chatbot request (\"Ask About Kapil\").
 *
 * @author Kapil Garg
 */
public record PortfolioChatRequest(

        @NotBlank(message = "Message is required")
        @Size(min = 1, max = 500, message = "Message must be between 1 and 500 characters")
        String message
) {
}
