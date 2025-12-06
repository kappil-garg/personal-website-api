package com.kapil.personalwebsite.service.impl;

import com.kapil.personalwebsite.dto.ContactRequest;
import com.kapil.personalwebsite.entity.PersonalInfo;
import com.kapil.personalwebsite.service.ContactService;
import com.kapil.personalwebsite.service.PersonalInfoService;
import com.kapil.personalwebsite.util.StringUtils;
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

    @Value("${contact.email.domain}")
    private String websiteDomain;

    public ContactServiceImpl(PersonalInfoService personalInfoService) {
        this.personalInfoService = personalInfoService;
    }

    @Override
    public String submitContact(ContactRequest contactRequest) {
        LOGGER.info("Processing contact form submission from: {}", contactRequest.getEmail());
        String toEmail = getRecipientEmail();
        if (toEmail == null || toEmail.isEmpty() || toEmail.equals("null")) {
            return handleEmailNotConfigured(contactRequest, "Recipient email not configured");
        }
        if (mailSender == null) {
            return handleEmailNotConfigured(contactRequest, "JavaMailSender not configured");
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(toEmail);
            message.setSubject(buildEmailSubject(contactRequest));
            message.setText(buildEmailBody(contactRequest));
            LOGGER.debug("Attempting to send email - From: {}, To: {}", senderEmail, toEmail);
            mailSender.send(message);
            LOGGER.info("Contact form email sent successfully to: {}", toEmail);
            return "Message sent successfully! I'll get back to you soon.";
        } catch (MailAuthenticationException e) {
            LOGGER.error("""
                    Email authentication failed. Ensure you're using an App Password (not regular password)
                    with 2FA enabled, and the username is the full email address. Error: {}
                    """, e.getMessage(), e);
            throw new RuntimeException("Email authentication failed. Please check your email configuration.", e);
        } catch (Exception e) {
            LOGGER.error("Failed to send contact form email", e);
            throw new RuntimeException("Failed to send message. Please try again later.", e);
        }
    }

    /**
     * Handles the case when email cannot be sent due to configuration issues.
     * Logs the contact submission and returns a user-friendly message.
     *
     * @param contactRequest the contact request data
     * @param reason         the reason why email cannot be sent
     * @return a user-friendly message
     */
    private String handleEmailNotConfigured(ContactRequest contactRequest, String reason) {
        LOGGER.info("Contact form submission - Name: {}, Email: {}, Subject: {}, Message: {}",
                contactRequest.getName(), contactRequest.getEmail(),
                contactRequest.getSubject(), contactRequest.getMessage());
        LOGGER.warn("{}. Contact form submission logged but email not sent.", reason);
        return "Thank you for your message. I'll get back to you soon.";
    }

    /**
     * Gets the recipient email address from configuration or personal info.
     *
     * @return the recipient email address, or null if not configured
     */
    private String getRecipientEmail() {
        if (recipientEmail != null && !recipientEmail.isEmpty() && !recipientEmail.equals("null")) {
            return recipientEmail;
        }
        return personalInfoService.getPersonalInfo()
                .map(PersonalInfo::getEmail)
                .orElse(null);
    }

    /**
     * Builds the email subject line.
     *
     * @param contactRequest the contact request data
     * @return the email subject line
     */
    private String buildEmailSubject(ContactRequest contactRequest) {
        String subjectPrefix = StringUtils.isNotBlank(contactRequest.getSubject())
                ? StringUtils.sanitizeForEmailHeader(contactRequest.getSubject())
                : "Contact Form Submission";
        String sanitizedName = StringUtils.sanitizeForEmailHeader(contactRequest.getName());
        return String.format("[Contact Form] %s - %s", subjectPrefix, sanitizedName);
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
        body.append("Name: ").append(StringUtils.sanitizeForEmailBody(contactRequest.getName())).append("\n");
        body.append("Email: ").append(StringUtils.sanitizeForEmailBody(contactRequest.getEmail())).append("\n");
        if (StringUtils.isNotBlank(contactRequest.getSubject())) {
            body.append("Subject: ").append(StringUtils.sanitizeForEmailBody(contactRequest.getSubject())).append("\n");
        }
        body.append("\nMessage:\n");
        body.append(StringUtils.sanitizeForEmailBody(contactRequest.getMessage())).append("\n\n");
        body.append("---\n");
        body.append("This message was sent from the contact form on ").append(websiteDomain);
        return body.toString();
    }

}
