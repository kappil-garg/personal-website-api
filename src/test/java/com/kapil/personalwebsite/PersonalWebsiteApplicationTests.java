package com.kapil.personalwebsite;

import com.kapil.personalwebsite.ai.blog.BlogAskService;
import com.kapil.personalwebsite.ai.contact.ContactPolishService;
import com.kapil.personalwebsite.ai.portfolio.PortfolioChatService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class PersonalWebsiteApplicationTests {

    /**
     * Mock the AI-dependent BlogAskService so that the application can start in tests without AI configuration.
     */
    @MockitoBean
    private BlogAskService blogAskService;

    @MockitoBean
    private ContactPolishService contactPolishService;

    @MockitoBean
    private PortfolioChatService portfolioChatService;

    @MockitoBean
    private EmbeddingModel embeddingModel;

    @Test
    void contextLoads() {

    }

}
