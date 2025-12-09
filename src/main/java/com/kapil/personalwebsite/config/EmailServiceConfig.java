package com.kapil.personalwebsite.config;

import com.kapil.personalwebsite.service.email.EmailService;
import com.kapil.personalwebsite.service.email.impl.HttpApiEmailService;
import com.kapil.personalwebsite.service.email.impl.SmtpEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration for email service selection.
 * Allows switching between SMTP and HTTP API via configuration.
 *
 * @author Kapil Garg
 */
@Configuration
public class EmailServiceConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailServiceConfig.class);

    /**
     * Creates the primary EmailService bean based on configuration.
     *
     * @param emailProvider       the email provider type (http-api or smtp)
     * @param httpApiEmailService the HTTP API email service
     * @param smtpEmailService    the SMTP email service
     * @return the configured email service
     */
    @Bean
    @Primary
    public EmailService emailService(@Value("${email.provider}") String emailProvider,
                                     HttpApiEmailService httpApiEmailService,
                                     SmtpEmailService smtpEmailService) {
        if ("smtp".equalsIgnoreCase(emailProvider)) {
            LOGGER.info("Using SMTP email service");
            return smtpEmailService;
        } else {
            LOGGER.info("Using HTTP API email service");
            return httpApiEmailService;
        }
    }

}
