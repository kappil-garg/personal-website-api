package com.kapil.personalwebsite.service.email.impl;

import com.kapil.personalwebsite.exception.EmailServiceException;
import com.kapil.personalwebsite.service.email.http.HttpEmailRequestBuilder;
import com.kapil.personalwebsite.service.email.http.HttpEmailRequestBuilderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HttpApiEmailServiceTest {

    private final String apiUrl = "http://api.test/send";

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private HttpEmailRequestBuilder requestBuilder;

    @Mock
    private HttpEmailRequestBuilderFactory requestBuilderFactory;

    private HttpApiEmailService emailService;

    @BeforeEach
    void setUp() {
        String apiKey = "api-key";
        when(requestBuilderFactory.get("brevo")).thenReturn(requestBuilder);
        emailService = new HttpApiEmailService(apiKey, apiUrl, "brevo", restTemplate, requestBuilderFactory);
    }

    @Test
    void sendContactEmail_WhenRequestSucceeds_ShouldCompleteWithoutException() {
        ResponseEntity<Map<String, Object>> response = ResponseEntity.ok(Map.of("status", "ok"));
        when(restTemplate.exchange(eq(apiUrl), eq(HttpMethod.POST), any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any())).thenReturn(response);
        when(requestBuilder.buildRequestBody(any(), any(), any(), any())).thenReturn(Map.of());
        assertDoesNotThrow(() -> emailService.sendContactEmail("to@example.com", "from@example.com",
                "Subject", "<p>Body</p>"));
    }

    @Test
    void sendContactEmail_WhenApiKeyMissing_ShouldThrowEmailServiceException() {
        when(requestBuilderFactory.get("brevo")).thenReturn(requestBuilder);
        emailService = new HttpApiEmailService("  ", apiUrl, "brevo", restTemplate, requestBuilderFactory);
        assertThrows(EmailServiceException.class, () -> emailService.sendContactEmail(
                "to@example.com", "from@example.com", "Subject", "<p>Body</p>"));
    }

    @Test
    void sendContactEmail_WhenResponseNot2xx_ShouldThrowEmailServiceException() {
        ResponseEntity<Map<String, Object>> response = ResponseEntity.status(500).body(Map.of());
        when(restTemplate.exchange(eq(apiUrl), eq(HttpMethod.POST), any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any())).thenReturn(response);
        when(requestBuilder.buildRequestBody(any(), any(), any(), any())).thenReturn(Map.of());
        assertThrows(EmailServiceException.class, () -> emailService.sendContactEmail(
                "to@example.com", "from@example.com", "Subject", "<p>Body</p>"));
    }

    @Test
    void sendContactEmail_WhenRestClientExceptionThrown_ShouldThrowEmailServiceException() {
        when(restTemplate.exchange(eq(apiUrl), eq(HttpMethod.POST), any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any()))
                .thenThrow(new RestClientException("down"));
        when(requestBuilder.buildRequestBody(any(), any(), any(), any())).thenReturn(Map.of());
        assertThrows(EmailServiceException.class, () -> emailService.sendContactEmail(
                "to@example.com", "from@example.com", "Subject", "<p>Body</p>"));
    }

}
