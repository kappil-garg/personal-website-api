package com.kapil.personalwebsite.controller;

import com.kapil.personalwebsite.ai.dto.PortfolioChatRequest;
import com.kapil.personalwebsite.ai.dto.PortfolioChatResponse;
import com.kapil.personalwebsite.ai.portfolio.PortfolioChatService;
import com.kapil.personalwebsite.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the portfolio chatbot (\"Ask About Kapil\").
 * Exposes an endpoint that accepts a visitor's question and returns an AI-generated answer using portfolio context.
 *
 * @author Kapil Garg
 */
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiChatController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiChatController.class);

    private final PortfolioChatService portfolioChatService;

    /**
     * Answers a visitor's question using AI with portfolio data as context.
     *
     * @param request the chat request containing the visitor's message
     * @return a ResponseEntity containing the AI reply
     */
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<PortfolioChatResponse>> chat(@Valid @RequestBody PortfolioChatRequest request) {
        LOGGER.info("POST /ai/chat - Portfolio chat requested");
        PortfolioChatResponse responseData = portfolioChatService.chat(request);
        ApiResponse<PortfolioChatResponse> response = ApiResponse.success(
                responseData,
                "Reply generated successfully"
        );
        return ResponseEntity.ok(response);
    }

}
