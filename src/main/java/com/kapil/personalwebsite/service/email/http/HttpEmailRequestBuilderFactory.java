package com.kapil.personalwebsite.service.email.http;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Factory to resolve HTTP email request builders by provider key.
 *
 * @author Kapil Garg
 */
@Component
public class HttpEmailRequestBuilderFactory {

    private final Map<String, HttpEmailRequestBuilder> requestBuilders;

    public HttpEmailRequestBuilderFactory(Map<String, HttpEmailRequestBuilder> requestBuilders) {
        this.requestBuilders = requestBuilders;
    }

    /**
     * Gets the HTTP email request builder for the specified provider.
     *
     * @param provider the email provider key
     * @return the corresponding HTTP email request builder
     * @throws IllegalStateException if no builder is registered for the provider
     */
    public HttpEmailRequestBuilder get(String provider) {
        String key = HttpEmailProvider.normalize(provider);
        HttpEmailRequestBuilder builder = requestBuilders.get(key);
        if (builder == null) {
            throw new IllegalStateException("No HTTP email request builder registered for provider: " + provider);
        }
        return builder;
    }

}
