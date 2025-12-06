package com.kapil.personalwebsite.service.impl;

import com.kapil.personalwebsite.dto.ContactRequest;
import com.kapil.personalwebsite.entity.PersonalInfo;
import com.kapil.personalwebsite.service.PersonalInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

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

    @InjectMocks
    private ContactServiceImpl contactService;

    private ContactRequest createContactRequest() {
        ContactRequest request = new ContactRequest();
        request.setName("Ted Mosby");
        request.setEmail("ted.mosby@example.com");
        request.setSubject("Test Subject");
        request.setMessage("Test message content");
        return request;
    }

    private PersonalInfo createPersonalInfo() {
        PersonalInfo info = new PersonalInfo();
        info.setEmail("recipient@example.com");
        return info;
    }

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(contactService, "recipientEmail", "");
        ReflectionTestUtils.setField(contactService, "senderEmail", "sender@example.com");
        ReflectionTestUtils.setField(contactService, "websiteDomain", "testdomain.com");
        ReflectionTestUtils.setField(contactService, "mailSender", mailSender);
    }

    @Test
    void submitContact_WhenEmailConfigured_ShouldSendEmailSuccessfully() {
        ContactRequest request = createContactRequest();
        ReflectionTestUtils.setField(contactService, "recipientEmail", "recipient@example.com");
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        String result = contactService.submitContact(request);
        assertEquals("Message sent successfully! I'll get back to you soon.", result);
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void submitContact_WhenRecipientEmailNotConfigured_ShouldUsePersonalInfoEmail() {
        ContactRequest request = createContactRequest();
        ReflectionTestUtils.setField(contactService, "recipientEmail", "");
        when(personalInfoService.getPersonalInfo()).thenReturn(Optional.of(createPersonalInfo()));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        String result = contactService.submitContact(request);
        assertEquals("Message sent successfully! I'll get back to you soon.", result);
        verify(mailSender).send(any(SimpleMailMessage.class));
        verify(personalInfoService).getPersonalInfo();
    }

    @Test
    void submitContact_WhenRecipientEmailIsNull_ShouldReturnFallbackMessage() {
        ContactRequest request = createContactRequest();
        ReflectionTestUtils.setField(contactService, "recipientEmail", "");
        when(personalInfoService.getPersonalInfo()).thenReturn(Optional.empty());
        String result = contactService.submitContact(request);
        assertEquals("Thank you for your message. I'll get back to you soon.", result);
        verify(personalInfoService).getPersonalInfo();
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void submitContact_WhenRecipientEmailIsNullString_ShouldReturnFallbackMessage() {
        ContactRequest request = createContactRequest();
        ReflectionTestUtils.setField(contactService, "recipientEmail", "null");
        when(personalInfoService.getPersonalInfo()).thenReturn(Optional.empty());
        String result = contactService.submitContact(request);
        assertEquals("Thank you for your message. I'll get back to you soon.", result);
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void submitContact_WhenMailSenderNotConfigured_ShouldReturnFallbackMessage() {
        ContactRequest request = createContactRequest();
        ReflectionTestUtils.setField(contactService, "recipientEmail", "recipient@example.com");
        ReflectionTestUtils.setField(contactService, "mailSender", null);
        String result = contactService.submitContact(request);
        assertEquals("Thank you for your message. I'll get back to you soon.", result);
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void submitContact_WhenSenderEmailNotConfigured_ShouldAttemptToSendEmail() {
        ContactRequest request = createContactRequest();
        ReflectionTestUtils.setField(contactService, "recipientEmail", "recipient@example.com");
        ReflectionTestUtils.setField(contactService, "senderEmail", "");
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        String result = contactService.submitContact(request);
        assertEquals("Message sent successfully! I'll get back to you soon.", result);
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void submitContact_WhenMailAuthenticationFails_ShouldThrowRuntimeException() {
        ContactRequest request = createContactRequest();
        ReflectionTestUtils.setField(contactService, "recipientEmail", "recipient@example.com");
        doThrow(new MailAuthenticationException("Authentication failed"))
                .when(mailSender).send(any(SimpleMailMessage.class));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> contactService.submitContact(request));
        assertEquals("Email authentication failed. Please check your email configuration.", exception.getMessage());
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void submitContact_WhenMailSendingFails_ShouldThrowRuntimeException() {
        ContactRequest request = createContactRequest();
        ReflectionTestUtils.setField(contactService, "recipientEmail", "recipient@example.com");
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> contactService.submitContact(request));
        assertEquals("Failed to send message. Please try again later.", exception.getMessage());
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void submitContact_WithNullSubject_ShouldHandleGracefully() {
        ContactRequest request = createContactRequest();
        request.setSubject(null);
        ReflectionTestUtils.setField(contactService, "recipientEmail", "recipient@example.com");
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        String result = contactService.submitContact(request);
        assertEquals("Message sent successfully! I'll get back to you soon.", result);
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void submitContact_WithEmptySubject_ShouldHandleGracefully() {
        ContactRequest request = createContactRequest();
        request.setSubject("");
        ReflectionTestUtils.setField(contactService, "recipientEmail", "recipient@example.com");
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        String result = contactService.submitContact(request);
        assertEquals("Message sent successfully! I'll get back to you soon.", result);
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void submitContact_WhenDomainNotConfigured_ShouldUseDefaultDomain() {
        ContactRequest request = createContactRequest();
        ReflectionTestUtils.setField(contactService, "recipientEmail", "recipient@example.com");
        ReflectionTestUtils.setField(contactService, "websiteDomain", "");
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        String result = contactService.submitContact(request);
        assertEquals("Message sent successfully! I'll get back to you soon.", result);
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

}
