package com.kapil.personalwebsite.service.email;

import com.kapil.personalwebsite.dto.ContactRequest;

/**
 * Service interface for sending emails via different providers.
 *
 * @author Kapil Garg
 */
public interface EmailService {

    /**
     * Sends a contact form email notification.
     *
     * @param contactRequest the contact form data
     * @param toEmail        the recipient email address
     * @param fromEmail      the sender email address
     * @param subject        the email subject
     * @param body           the email body content
     * @throws Exception if email sending fails
     */
    void sendContactEmail(ContactRequest contactRequest, String toEmail, String fromEmail,
                          String subject, String body) throws Exception;

}
