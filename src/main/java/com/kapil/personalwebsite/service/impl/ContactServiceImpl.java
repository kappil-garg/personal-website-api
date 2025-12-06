package com.kapil.personalwebsite.service.impl;

import com.kapil.personalwebsite.dto.ContactRequest;
import com.kapil.personalwebsite.entity.PersonalInfo;
import com.kapil.personalwebsite.service.ContactService;
import com.kapil.personalwebsite.service.PersonalInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Implementation of ContactService for handling contact form submissions and email notifications.
 *
 * @author Kapil Garg
 */
@Service
public class ContactServiceImpl implements ContactService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContactServiceImpl.class);

    private final PersonalInfoService personalInfoService;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${contact.email.to}")
    private String recipientEmail;

    @Value("${contact.email.from}")
    private String senderEmail;

    @Value("${spring.mail.username}")
    private String mailUsername;

    public ContactServiceImpl(PersonalInfoService personalInfoService) {
        this.personalInfoService = personalInfoService;
    }

    @Override
    public String submitContact(ContactRequest contactRequest) {
        LOGGER.info("Processing contact form submission from: {}", contactRequest.getEmail());
        String toEmail = getRecipientEmail();
        if (toEmail == null || toEmail.isEmpty()) {
            LOGGER.warn("Recipient email not configured. Contact form submission logged but email not sent.");
            LOGGER.info("Contact form submission - Name: {}, Email: {}, Subject: {}, Message: {}",
                    contactRequest.getName(), contactRequest.getEmail(),
                    contactRequest.getSubject(), contactRequest.getMessage());
            return "Thank you for your message. I'll get back to you soon.";
        }
        if (mailSender == null) {
            LOGGER.warn("JavaMailSender not configured. Contact form submission logged but email not sent.");
            LOGGER.info("Contact form submission - Name: {}, Email: {}, Subject: {}, Message: {}",
                    contactRequest.getName(), contactRequest.getEmail(),
                    contactRequest.getSubject(), contactRequest.getMessage());
            return "Thank you for your message. I'll get back to you soon.";
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            String fromEmail = getSenderEmail();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(buildEmailSubject(contactRequest));
            message.setText(buildEmailBody(contactRequest));
            LOGGER.debug("Attempting to send email - From: {}, To: {}", fromEmail, toEmail);
            mailSender.send(message);
            LOGGER.info("Contact form email sent successfully to: {}", toEmail);
            return "Message sent successfully! I'll get back to you soon.";
        } catch (MailAuthenticationException e) {
            LOGGER.error("Gmail authentication failed. Common causes:", e);
            LOGGER.error("1. Not using App Password (must use App Password, not regular password)");
            LOGGER.error("2. 2FA not enabled on Gmail account (required for App Passwords)");
            LOGGER.error("3. Incorrect username format (should be full email: your-email@gmail.com)");
            LOGGER.error("4. App Password copied incorrectly (check for spaces or extra characters)");
            LOGGER.error("5. Username configured: {}", mailUsername != null && !mailUsername.isEmpty() ? mailUsername : "NOT SET");
            throw new RuntimeException("Email authentication failed. Please check your email configuration.", e);
        } catch (Exception e) {
            LOGGER.error("Failed to send contact form email", e);
            throw new RuntimeException("Failed to send message. Please try again later.", e);
        }
    }

    /**
     * Gets the recipient email address from configuration or personal info.
     *
     * @return the recipient email address
     */
    private String getRecipientEmail() {
        if (recipientEmail != null && !recipientEmail.isEmpty()) {
            return recipientEmail;
        }
        return personalInfoService.getPersonalInfo()
                .map(PersonalInfo::getEmail)
                .orElse(null);
    }

    /**
     * Gets the sender email address from configuration.
     *
     * @return the sender email address
     */
    private String getSenderEmail() {
        if (senderEmail != null && !senderEmail.isEmpty()) {
            return senderEmail;
        }
        if (mailUsername != null && !mailUsername.isEmpty()) {
            return mailUsername;
        }
        return "noreply@kappilgarg.dev";
    }

    /**
     * Builds the email subject line.
     *
     * @param contactRequest the contact request data
     * @return the email subject line
     */
    private String buildEmailSubject(ContactRequest contactRequest) {
        String subjectPrefix = contactRequest.getSubject() != null && !contactRequest.getSubject().isEmpty()
                ? contactRequest.getSubject()
                : "Contact Form Submission";
        return String.format("[Contact Form] %s - %s", subjectPrefix, contactRequest.getName());
    }

    /**
     * Builds the email body content.
     *
     * @param contactRequest the contact request data
     * @return the email body content
     */
    private String buildEmailBody(ContactRequest contactRequest) {
        StringBuilder body = new StringBuilder();
        body.append("You have received a new contact form submission:\n\n");
        body.append("Name: ").append(contactRequest.getName()).append("\n");
        body.append("Email: ").append(contactRequest.getEmail()).append("\n");
        if (contactRequest.getSubject() != null && !contactRequest.getSubject().isEmpty()) {
            body.append("Subject: ").append(contactRequest.getSubject()).append("\n");
        }
        body.append("\nMessage:\n");
        body.append(contactRequest.getMessage()).append("\n\n");
        body.append("---\n");
        body.append("This message was sent from the contact form on kappilgarg.dev");
        return body.toString();
    }

}
