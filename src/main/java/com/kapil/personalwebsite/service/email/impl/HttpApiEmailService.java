package com.kapil.personalwebsite.service.email.impl;

import com.kapil.personalwebsite.dto.ContactRequest;
import com.kapil.personalwebsite.service.email.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of EmailService using HTTP API.
 *
 * @author Kapil Garg
 */
@Service
public class HttpApiEmailService implements EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpApiEmailService.class);

    private final RestTemplate restTemplate;

    private final String apiKey;
    private final String apiUrl;

    public HttpApiEmailService(@Value("${email.http-api.api-key}") String apiKey,
                               @Value("${email.http-api.url}") String apiUrl) {
        this.restTemplate = new RestTemplate();
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
    }

    @Override
    public void sendContactEmail(ContactRequest contactRequest, String toEmail, String fromEmail,
                                 String subject, String body) throws Exception {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("HTTP API key is not configured");
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);
            Map<String, Object> requestBody = buildRequestBody(toEmail, fromEmail, subject, body);
            ResponseEntity<Map<String, Object>> response = sendEmailRequest(requestBody, headers);
            if (!response.getStatusCode().is2xxSuccessful()) {
                LOGGER.warn("HTTP API returned non-2xx status: {}", response.getStatusCode());
                throw new Exception("Email sending failed with status: " + response.getStatusCode());
            }
            LOGGER.info("Contact form email sent successfully via HTTP API");
        } catch (RestClientException e) {
            LOGGER.error("Failed to send email via HTTP API", e);
            throw new Exception("Failed to send email via HTTP API: " + e.getMessage(), e);
        }
    }

    /**
     * Builds the request body for the email API.
     *
     * @param toEmail   the recipient email address
     * @param fromEmail the sender email address
     * @param subject   the email subject
     * @param body      the email body content
     * @return the request body map
     */
    private Map<String, Object> buildRequestBody(String toEmail, String fromEmail, String subject, String body) {
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, String> sender = new HashMap<>();
        sender.put("email", fromEmail);
        requestBody.put("sender", sender);
        Map<String, String> to = new HashMap<>();
        to.put("email", toEmail);
        requestBody.put("to", List.of(to));
        requestBody.put("subject", subject);
        requestBody.put("htmlContent", body);
        return requestBody;
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
