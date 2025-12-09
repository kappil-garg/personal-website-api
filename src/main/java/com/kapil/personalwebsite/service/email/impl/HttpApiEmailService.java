package com.kapil.personalwebsite.service.email.impl;

import com.kapil.personalwebsite.exception.EmailServiceException;
import com.kapil.personalwebsite.service.email.EmailService;
import com.kapil.personalwebsite.service.email.http.HttpEmailProvider;
import com.kapil.personalwebsite.service.email.http.HttpEmailRequestBuilder;
import com.kapil.personalwebsite.service.email.http.HttpEmailRequestBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Implementation of EmailService using HTTP API.
 *
 * @author Kapil Garg
 */
@Service
public class HttpApiEmailService implements EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpApiEmailService.class);

    private final String apiKey;
    private final String apiUrl;

    private final RestTemplate restTemplate;
    private final HttpEmailRequestBuilder requestBuilder;

    public HttpApiEmailService(@Value("${email.http-api.api-key}") String apiKey,
                               @Value("${email.http-api.url}") String apiUrl,
                               @Value("${email.http-api.provider:brevo}") String provider,
                               RestTemplate restTemplate,
                               HttpEmailRequestBuilderFactory requestBuilderFactory) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.restTemplate = restTemplate;
        String resolvedProvider = provider == null || provider.isBlank() ? HttpEmailProvider.BREVO : provider;
        this.requestBuilder = requestBuilderFactory.get(resolvedProvider);
    }

    @Override
    public void sendContactEmail(String toEmail, String fromEmail, String subject, String body) throws EmailServiceException {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new EmailServiceException("HTTP API key is not configured");
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);
            Map<String, Object> requestBody = requestBuilder.buildRequestBody(toEmail, fromEmail, subject, body);
            ResponseEntity<Map<String, Object>> response = sendEmailRequest(requestBody, headers);
            if (!response.getStatusCode().is2xxSuccessful()) {
                LOGGER.warn("HTTP API returned non-2xx status: {}", response.getStatusCode());
                throw new EmailServiceException("Email sending failed with status: " + response.getStatusCode());
            }
            LOGGER.info("Contact form email sent successfully via HTTP API");
        } catch (RestClientException e) {
            LOGGER.error("Failed to send email via HTTP API", e);
            throw new EmailServiceException("Failed to send email via HTTP API", e);
        }
    }

    /**
     * Sends the email request to the HTTP API.
     *
     * @param requestBody the request body map
     * @param headers     the HTTP headers
     * @return the response entity
     */
    private ResponseEntity<Map<String, Object>> sendEmailRequest(Map<String, Object> requestBody, HttpHeaders headers) {
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ParameterizedTypeReference<Map<String, Object>> responseType = new ParameterizedTypeReference<>() {
        };
        return restTemplate.exchange(apiUrl, HttpMethod.POST, request, responseType);
    }

}
