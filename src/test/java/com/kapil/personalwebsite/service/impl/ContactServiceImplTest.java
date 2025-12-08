package com.kapil.personalwebsite.service.impl;

import com.kapil.personalwebsite.dto.ContactRequest;
import com.kapil.personalwebsite.exception.EmailSendingException;
import com.kapil.personalwebsite.service.PersonalInfoService;
import com.kapil.personalwebsite.util.AppConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactServiceImplTest {

    @Mock
    private PersonalInfoService personalInfoService;

    @Mock
    private JavaMailSender mailSender;

    private ContactServiceImpl contactService;

    private ContactRequest createContactRequest() {
        ContactRequest request = new ContactRequest();
        request.setName("Ted Mosby");
        request.setEmail("ted.mosby@example.com");
        request.setSubject("Test Subject");
        request.setMessage("Test message content");
        return request;
    }

    private ContactServiceImpl createContactService(String recipientEmail, String senderEmail,
                                                    String websiteDomain, JavaMailSender mailSender) {
        return new ContactServiceImpl(personalInfoService, mailSender, senderEmail, recipientEmail, websiteDomain);
    }

    @BeforeEach
    void setUp() {
        contactService = createContactService("", "sender@example.com", "testdomain.com", mailSender);
    }

    @Test
    void submitContact_WhenEmailConfigured_ShouldSendEmailSuccessfully() {
        ContactRequest request = createContactRequest();
        contactService = createContactService("recipient@example.com", "sender@example.com", "testdomain.com", mailSender);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        String result = contactService.submitContact(request);
        assertEquals("Message sent successfully! I'll get back to you soon.", result);
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void submitContact_WhenMailSenderNotConfigured_ShouldReturnFallbackMessage() {
        ContactRequest request = createContactRequest();
        contactService = createContactService("recipient@example.com", "sender@example.com", "testdomain.com", null);
        String result = contactService.submitContact(request);
        assertEquals(AppConstants.THANK_YOU_FOR_CONTACTING, result);
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void submitContact_WhenSenderEmailNotConfigured_ShouldReturnFallbackMessage() {
        ContactRequest request = createContactRequest();
        contactService = createContactService("recipient@example.com", "", "testdomain.com", mailSender);
        String result = contactService.submitContact(request);
        assertEquals(AppConstants.THANK_YOU_FOR_CONTACTING, result);
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void submitContact_WhenMailAuthenticationFails_ShouldThrowEmailSendingException() {
        ContactRequest request = createContactRequest();
        contactService = createContactService("recipient@example.com", "sender@example.com", "testdomain.com", mailSender);
        doThrow(new MailAuthenticationException("Authentication failed"))
                .when(mailSender).send(any(SimpleMailMessage.class));
        EmailSendingException exception = assertThrows(EmailSendingException.class,
                () -> contactService.submitContact(request));
        assertEquals("Email authentication failed. Please check your email configuration.", exception.getMessage());
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void submitContact_WhenMailSendingFails_ShouldThrowEmailSendingException() {
        ContactRequest request = createContactRequest();
        contactService = createContactService("recipient@example.com", "sender@example.com", "testdomain.com", mailSender);
        doThrow(new RuntimeException("SMTP error"))
                .when(mailSender).send(any(SimpleMailMessage.class));
        EmailSendingException exception = assertThrows(EmailSendingException.class,
                () -> contactService.submitContact(request));
        assertEquals("Failed to send message. Please try again later.", exception.getMessage());
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void submitContact_WithNullSubject_ShouldHandleGracefully() {
        ContactRequest request = createContactRequest();
        request.setSubject(null);
        contactService = createContactService("recipient@example.com", "sender@example.com", "testdomain.com", mailSender);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        String result = contactService.submitContact(request);
        assertEquals("Message sent successfully! I'll get back to you soon.", result);
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void submitContact_WithEmptySubject_ShouldHandleGracefully() {
        ContactRequest request = createContactRequest();
        request.setSubject("");
        contactService = createContactService("recipient@example.com", "sender@example.com", "testdomain.com", mailSender);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        String result = contactService.submitContact(request);
        assertEquals("Message sent successfully! I'll get back to you soon.", result);
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void submitContact_WhenDomainNotConfigured_ShouldUseDefaultDomain() {
        ContactRequest request = createContactRequest();
        contactService = createContactService("recipient@example.com", "sender@example.com", "", mailSender);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        String result = contactService.submitContact(request);
        assertEquals("Message sent successfully! I'll get back to you soon.", result);
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

}
