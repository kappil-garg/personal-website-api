package com.kapil.personalwebsite.service.email;

import com.kapil.personalwebsite.exception.EmailServiceException;

/**
 * Service interface for sending emails via different providers.
 *
 * @author Kapil Garg
 */
public interface EmailService {

    /**
     * Sends a contact form email notification.
     *
     * @param toEmail   the recipient email address
     * @param fromEmail the sender email address
     * @param subject   the email subject
     * @param body      the email body content
     * @throws EmailServiceException if email sending fails
     */
    void sendContactEmail(String toEmail, String fromEmail, String subject, String body) throws EmailServiceException;

}
