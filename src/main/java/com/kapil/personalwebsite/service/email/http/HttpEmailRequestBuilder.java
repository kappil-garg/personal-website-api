package com.kapil.personalwebsite.service.email.http;

import java.util.Map;

/**
 * Strategy for building HTTP email provider request payloads.
 *
 * @author Kapil Garg
 */
public interface HttpEmailRequestBuilder {

    /**
     * Builds provider-specific request payload.
     *
     * @param toEmail   recipient email
     * @param fromEmail sender email
     * @param subject   email subject
     * @param body      email body (HTML)
     * @return provider-specific request map
     */
    Map<String, Object> buildRequestBody(String toEmail, String fromEmail, String subject, String body);

}
