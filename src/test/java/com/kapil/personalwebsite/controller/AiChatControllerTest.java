package com.kapil.personalwebsite.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kapil.personalwebsite.ai.dto.PortfolioChatRequest;
import com.kapil.personalwebsite.ai.dto.PortfolioChatResponse;
import com.kapil.personalwebsite.ai.portfolio.PortfolioChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AiChatController (portfolio chatbot endpoint).
 *
 * @author Kapil Garg
 */
@ExtendWith(MockitoExtension.class)
class AiChatControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private MockMvc mockMvc;
    @Mock
    private PortfolioChatService portfolioChatService;

    @InjectMocks
    private AiChatController aiChatController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(aiChatController).build();
    }

    @Test
    @DisplayName("POST /ai/chat returns 200 and reply when service returns response")
    void chat_ValidRequest_ReturnsOkAndReply() throws Exception {
        String userMessage = "What are Kapil's key skills?";
        PortfolioChatRequest request = new PortfolioChatRequest(userMessage);
        PortfolioChatResponse responseData = new PortfolioChatResponse("Kapil has experience in Java, Spring, Angular, and cloud technologies.");
        when(portfolioChatService.chat(any(PortfolioChatRequest.class))).thenReturn(responseData);

        mockMvc.perform(post("/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reply").value(responseData.reply()));

        verify(portfolioChatService).chat(any(PortfolioChatRequest.class));
    }

    @Test
    @DisplayName("POST /ai/chat with empty message is rejected by validation")
    void chat_EmptyMessage_ReturnsBadRequest() throws Exception {
        PortfolioChatRequest request = new PortfolioChatRequest("   ");

        mockMvc.perform(post("/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /ai/chat with missing message is rejected by validation")
    void chat_MissingMessage_ReturnsBadRequest() throws Exception {
        String json = "{}";

        mockMvc.perform(post("/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /ai/chat with message over 500 characters is rejected by validation")
    void chat_MessageTooLong_ReturnsBadRequest() throws Exception {
        String longMessage = "a".repeat(501);
        PortfolioChatRequest request = new PortfolioChatRequest(longMessage);

        mockMvc.perform(post("/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

}
