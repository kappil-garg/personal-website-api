package com.kapil.personalwebsite.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class for setting up CORS (Cross-Origin Resource Sharing) settings.
 *
 * @author Kapil Garg
 */
@Configuration
public class CorsConfig {

    private final long maxAge;

    private final String allowedOrigins;
    private final String allowedMethods;

    private final boolean allowCredentials;

    public CorsConfig(@Value("${cors.max-age}") long maxAge,
                      @Value("${cors.allowed-origins}") String allowedOrigins,
                      @Value("${cors.allowed-methods}") String allowedMethods,
                      @Value("${cors.allow-credentials}") boolean allowCredentials) {
        this.maxAge = maxAge;
        this.allowedOrigins = allowedOrigins;
        this.allowedMethods = allowedMethods;
        this.allowCredentials = allowCredentials;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                if (allowedOrigins != null && !allowedOrigins.equals("null")) {
                    registry.addMapping("/api/**")
                            .allowedOrigins(allowedOrigins.split(","))
                            .allowedMethods(allowedMethods.split(","))
                            .allowedHeaders("*")
                            .allowCredentials(allowCredentials)
                            .maxAge(maxAge);
                }
            }
        };
    }

}
