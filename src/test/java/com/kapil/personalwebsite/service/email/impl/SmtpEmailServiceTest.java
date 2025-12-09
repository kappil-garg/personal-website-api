package com.kapil.personalwebsite.service.email.impl;

import com.kapil.personalwebsite.exception.EmailServiceException;
import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmtpEmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private SmtpEmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new SmtpEmailService(mailSender);
    }

    @Test
    void sendContactEmail_WhenSenderConfigured_ShouldSend() {
        MimeMessage message = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(message);
        doNothing().when(mailSender).send(any(MimeMessage.class));
        assertDoesNotThrow(() -> emailService.sendContactEmail("to@example.com", "from@example.com",
                "Subject", "<p>Body</p>"));
    }

    @Test
    void sendContactEmail_WhenMailSenderMissing_ShouldThrowEmailServiceException() {
        emailService = new SmtpEmailService(null);
        assertThrows(EmailServiceException.class, () -> emailService.sendContactEmail(
                "to@example.com", "from@example.com", "Subject", "<p>Body</p>"));
    }

    @Test
    void sendContactEmail_WhenAuthenticationFails_ShouldThrowEmailServiceException() {
        MimeMessage message = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(message);
        doThrow(new MailAuthenticationException("auth failed")).when(mailSender).send(any(MimeMessage.class));
        assertThrows(EmailServiceException.class, () -> emailService.sendContactEmail(
                "to@example.com", "from@example.com", "Subject", "<p>Body</p>"));
    }

    @Test
    void sendContactEmail_WhenMessagingExceptionOccurs_ShouldThrowEmailServiceException() {
        when(mailSender.createMimeMessage()).thenReturn(new FailingMimeMessage());
        assertThrows(EmailServiceException.class, () -> emailService.sendContactEmail(
                "to@example.com", "from@example.com", "Subject", "<p>Body</p>"));
    }

    @Test
    void sendContactEmail_WhenUnexpectedErrorOccurs_ShouldThrowEmailServiceException() {
        MimeMessage message = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(message);
        doThrow(new RuntimeException("boom")).when(mailSender).send(any(MimeMessage.class));
        assertThrows(EmailServiceException.class, () -> emailService.sendContactEmail(
                "to@example.com", "from@example.com", "Subject", "<p>Body</p>"));
    }

    /**
     * MimeMessage that throws MessagingException when setting the sender.
     */
    private static class FailingMimeMessage extends MimeMessage {
        FailingMimeMessage() {
            super((Session) null);
        }
        @Override
        public void setFrom(Address address) throws MessagingException {
            throw new MessagingException("failed to set from");
        }
    }

}
