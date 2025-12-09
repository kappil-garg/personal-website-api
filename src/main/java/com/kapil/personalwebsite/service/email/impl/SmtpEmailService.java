package com.kapil.personalwebsite.service.email.impl;

import com.kapil.personalwebsite.exception.EmailServiceException;
import com.kapil.personalwebsite.service.email.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Implementation of EmailService using SMTP (JavaMailSender).
 *
 * @author Kapil Garg
 */
@Service
public class SmtpEmailService implements EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpEmailService.class);

    @Nullable
    private final JavaMailSender mailSender;

    public SmtpEmailService(@Nullable JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendContactEmail(String toEmail, String fromEmail, String subject, String body) throws EmailServiceException {
        if (mailSender == null) {
            throw new EmailServiceException("JavaMailSender is not configured. SMTP email service cannot be used.");
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
            LOGGER.info("Contact form email sent successfully via SMTP");
        } catch (MailAuthenticationException e) {
            LOGGER.error("SMTP email authentication failed", e);
            throw new EmailServiceException("Email authentication failed. Please check your email configuration.", e);
        } catch (MessagingException e) {
            LOGGER.error("Failed to create email message", e);
            throw new EmailServiceException("Failed to create email message", e);
        } catch (Exception e) {
            LOGGER.error("Failed to send email via SMTP", e);
            throw new EmailServiceException("Failed to send email via SMTP", e);
        }
    }

}
