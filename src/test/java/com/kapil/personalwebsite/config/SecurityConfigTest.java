package com.kapil.personalwebsite.config;

import com.kapil.personalwebsite.ai.blog.BlogAskService;
import com.kapil.personalwebsite.ai.contact.ContactPolishService;
import com.kapil.personalwebsite.ai.portfolio.PortfolioChatService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Security configuration tests to verify security setup.
 * Tests that security configuration loads properly and beans are created.
 *
 * @author Kapil Garg
 */
@SpringBootTest
@ActiveProfiles("test")
class SecurityConfigTest {

    @MockitoBean
    private BlogAskService blogAskService;

    @MockitoBean
    private ContactPolishService contactPolishService;

    @MockitoBean
    private PortfolioChatService portfolioChatService;

    @MockitoBean
    private EmbeddingModel embeddingModel;

    @Autowired
    private SecurityConfig securityConfig;

    @Test
    void securityConfiguration_ShouldLoadSuccessfully() {
        assertNotNull(securityConfig, "SecurityConfig should be loaded");
    }

    @Test
    void securityConfiguration_ShouldHaveCorrectAnnotations() {
        EnableWebSecurity annotation = SecurityConfig.class.getAnnotation(EnableWebSecurity.class);
        assertNotNull(annotation, "SecurityConfig should have @EnableWebSecurity annotation");
    }

}
