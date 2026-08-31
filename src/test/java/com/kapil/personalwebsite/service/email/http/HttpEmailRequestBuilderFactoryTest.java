package com.kapil.personalwebsite.service.email.http;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HttpEmailRequestBuilderFactoryTest {

    @Test
    void get_ShouldNormalizeProviderKeyBeforeLookup() {
        HttpEmailRequestBuilder builder = (toEmail, fromEmail, subject, body) -> Map.of();
        HttpEmailRequestBuilderFactory factory = new HttpEmailRequestBuilderFactory(Map.of("brevo", builder));
        HttpEmailRequestBuilder result = factory.get("  BrEvO ");
        assertSame(builder, result);
    }

    @Test
    void get_WhenProviderKeyMatchesExactly_ShouldReturnBuilder() {
        HttpEmailRequestBuilder builder = (toEmail, fromEmail, subject, body) -> Map.of();
        HttpEmailRequestBuilderFactory factory = new HttpEmailRequestBuilderFactory(Map.of("brevo", builder));
        assertSame(builder, factory.get("brevo"));
    }

    @Test
    void get_WhenProviderIsMissing_ShouldThrowExceptionWithOriginalProvider() {
        HttpEmailRequestBuilderFactory factory = new HttpEmailRequestBuilderFactory(Map.of());
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> factory.get("SendGrid"));
        assertEquals("No HTTP email request builder registered for provider: SendGrid", exception.getMessage());
    }

}
