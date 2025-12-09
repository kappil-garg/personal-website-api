package com.kapil.personalwebsite.service.email.http;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Brevo-specific HTTP email request builder.
 *
 * @author Kapil Garg
 */
@Component("brevo")
public class BrevoEmailRequestBuilder implements HttpEmailRequestBuilder {

    @Override
    public Map<String, Object> buildRequestBody(String toEmail, String fromEmail, String subject, String body) {
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

}
