package com.kapil.personalwebsite.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.ActiveProfiles;

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
