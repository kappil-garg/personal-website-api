package com.kapil.personalwebsite.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for "ask about this blog post" request (AI Q&A).
 *
 * @author Kapil Garg
 */
public record BlogAskRequest(
        @NotBlank(message = "Question is required")
        @Size(min = 1, max = 500, message = "Question must be between 1 and 500 characters")
        String question
) {
}
