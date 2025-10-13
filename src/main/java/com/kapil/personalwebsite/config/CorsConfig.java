package com.kapil.personalwebsite.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${cors.allowed-methods}")
    private String allowedMethods;

    @Value("${cors.allow-credentials}")
    private boolean allowCredentials;

    @Value("${cors.max-age}")
    private long maxAge;

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
