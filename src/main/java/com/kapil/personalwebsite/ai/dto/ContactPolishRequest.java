package com.kapil.personalwebsite.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for contact message polish (AI writing assistant) request.
 *
 * @author Kapil Garg
 */
public record ContactPolishRequest(
        @NotBlank(message = "Message is required")
        @Size(min = 10, max = 2000, message = "Message must be between 10 and 2000 characters")
        String message
) {
}
