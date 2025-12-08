package com.kapil.personalwebsite.service.impl;

import com.kapil.personalwebsite.dto.ContactRequest;
import com.kapil.personalwebsite.entity.PersonalInfo;
import com.kapil.personalwebsite.exception.EmailSendingException;
import com.kapil.personalwebsite.service.ContactService;
import com.kapil.personalwebsite.service.PersonalInfoService;
import com.kapil.personalwebsite.util.AppConstants;
import com.kapil.personalwebsite.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
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

    @Nullable
    private final JavaMailSender mailSender;

    private final String senderEmail;
    private final String recipientEmail;
    private final String websiteDomain;

    public ContactServiceImpl(PersonalInfoService personalInfoService, @Nullable JavaMailSender mailSender,
                              @Value("${contact.email.from}") String senderEmail,
                              @Value("${contact.email.to}") String recipientEmail,
                              @Value("${contact.email.domain}") String websiteDomain) {
        this.personalInfoService = personalInfoService;
        this.mailSender = mailSender;
        this.senderEmail = senderEmail;
        this.recipientEmail = recipientEmail;
        this.websiteDomain = websiteDomain;
    }

    @Override
    public String submitContact(ContactRequest contactRequest) {
        String toEmail = getRecipientEmail();
        if (!isConfiguredEmail(toEmail)) {
            return handleEmailNotConfigured(AppConstants.RECIPIENT_EMAIL_NOT_CONFIGURED);
        }
        if (mailSender == null) {
            return handleEmailNotConfigured(AppConstants.JAVA_MAIL_SENDER_NOT_CONFIGURED);
        }
        if (!isConfiguredEmail(senderEmail)) {
            return handleEmailNotConfigured(AppConstants.SENDER_EMAIL_NOT_CONFIGURED);
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(toEmail);
            message.setSubject(buildEmailSubject(contactRequest));
            message.setText(buildEmailBody(contactRequest));
            mailSender.send(message);
            return AppConstants.MESSAGE_SENT_SUCCESS;
        } catch (MailAuthenticationException e) {
            LOGGER.error("Email authentication failed. Ensure you're using an App Password with 2FA enabled. Error: {}",
                    e.getMessage(), e);
            throw new EmailSendingException("Email authentication failed. Please check your email configuration.", e);
        } catch (Exception e) {
            LOGGER.error("Failed to send contact form email", e);
            throw new EmailSendingException("Failed to send message. Please try again later.", e);
        }
    }

    /**
     * Handles the case when email cannot be sent due to configuration issues.
     * Logs the contact submission and returns a user-friendly message.
     *
     * @param reason the reason why email cannot be sent
     * @return a user-friendly message
     */
    private String handleEmailNotConfigured(String reason) {
        LOGGER.warn("{}. Contact form submission logged but email not sent.", reason);
        return AppConstants.THANK_YOU_FOR_CONTACTING;
    }

    /**
     * Gets the recipient email address from configuration or personal info.
     *
     * @return the recipient email address, or null if not configured
     */
    private String getRecipientEmail() {
        if (isConfiguredEmail(recipientEmail)) {
            return recipientEmail;
        }
        return personalInfoService.getPersonalInfo()
                .map(PersonalInfo::getEmail)
                .orElse(null);
    }

    /**
     * Checks if an email address is properly configured (not null, not empty, not "null" string).
     *
     * @param email the email address to check
     * @return true if the email is configured, false otherwise
     */
    private boolean isConfiguredEmail(String email) {
        return StringUtils.isNotBlank(email) && !email.equals("null");
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
        body.append("This message was sent from the contact form on ")
                .append(StringUtils.sanitizeForEmailBody(websiteDomain));
        return body.toString();
    }

}
