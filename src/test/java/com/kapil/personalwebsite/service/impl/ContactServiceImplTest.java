package com.kapil.personalwebsite.service.impl;

import com.kapil.personalwebsite.dto.ContactRequest;
import com.kapil.personalwebsite.exception.EmailSendingException;
import com.kapil.personalwebsite.service.PersonalInfoService;
import com.kapil.personalwebsite.service.email.EmailService;
import com.kapil.personalwebsite.util.AppConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactServiceImplTest {

    @Mock
    private PersonalInfoService personalInfoService;

    @Mock
    private EmailService emailService;

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
                                                    String websiteDomain, EmailService emailService) {
        return new ContactServiceImpl(personalInfoService, emailService, senderEmail, recipientEmail, websiteDomain);
    }

    @BeforeEach
    void setUp() {
        contactService = createContactService("", "sender@example.com", "testdomain.com", emailService);
    }

    @Test
    void submitContact_WhenEmailConfigured_ShouldSendEmailSuccessfully() throws Exception {
        ContactRequest request = createContactRequest();
        contactService = createContactService("recipient@example.com", "sender@example.com", "testdomain.com", emailService);
        doNothing().when(emailService).sendContactEmail(any(), anyString(), anyString(), anyString(), anyString());
        String result = contactService.submitContact(request);
        assertEquals("Message sent successfully! I'll get back to you soon.", result);
        verify(emailService).sendContactEmail(any(ContactRequest.class), eq("recipient@example.com"),
                eq("sender@example.com"), anyString(), anyString());
    }

    @Test
    void submitContact_WhenEmailServiceNotConfigured_ShouldReturnFallbackMessage() throws Exception {
        ContactRequest request = createContactRequest();
        contactService = createContactService("recipient@example.com", "sender@example.com", "testdomain.com", null);
        String result = contactService.submitContact(request);
        assertEquals(AppConstants.THANK_YOU_FOR_CONTACTING, result);
        verify(emailService, never()).sendContactEmail(any(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void submitContact_WhenSenderEmailNotConfigured_ShouldReturnFallbackMessage() throws Exception {
        ContactRequest request = createContactRequest();
        contactService = createContactService("recipient@example.com", "", "testdomain.com", emailService);
        String result = contactService.submitContact(request);
        assertEquals(AppConstants.THANK_YOU_FOR_CONTACTING, result);
        verify(emailService, never()).sendContactEmail(any(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void submitContact_WhenEmailSendingFails_ShouldThrowEmailSendingException() throws Exception {
        ContactRequest request = createContactRequest();
        contactService = createContactService("recipient@example.com", "sender@example.com", "testdomain.com", emailService);
        doThrow(new Exception("Email sending failed"))
                .when(emailService).sendContactEmail(any(), anyString(), anyString(), anyString(), anyString());
        EmailSendingException exception = assertThrows(EmailSendingException.class,
                () -> contactService.submitContact(request));
        assertEquals("Failed to send message. Please try again later.", exception.getMessage());
        verify(emailService).sendContactEmail(any(ContactRequest.class), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void submitContact_WithNullSubject_ShouldHandleGracefully() throws Exception {
        ContactRequest request = createContactRequest();
        request.setSubject(null);
        contactService = createContactService("recipient@example.com", "sender@example.com", "testdomain.com", emailService);
        doNothing().when(emailService).sendContactEmail(any(), anyString(), anyString(), anyString(), anyString());
        String result = contactService.submitContact(request);
        assertEquals("Message sent successfully! I'll get back to you soon.", result);
        verify(emailService).sendContactEmail(any(ContactRequest.class), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void submitContact_WithEmptySubject_ShouldHandleGracefully() throws Exception {
        ContactRequest request = createContactRequest();
        request.setSubject("");
        contactService = createContactService("recipient@example.com", "sender@example.com", "testdomain.com", emailService);
        doNothing().when(emailService).sendContactEmail(any(), anyString(), anyString(), anyString(), anyString());
        String result = contactService.submitContact(request);
        assertEquals("Message sent successfully! I'll get back to you soon.", result);
        verify(emailService).sendContactEmail(any(ContactRequest.class), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void submitContact_WhenDomainNotConfigured_ShouldUseDefaultDomain() throws Exception {
        ContactRequest request = createContactRequest();
        contactService = createContactService("recipient@example.com", "sender@example.com", "", emailService);
        doNothing().when(emailService).sendContactEmail(any(), anyString(), anyString(), anyString(), anyString());
        String result = contactService.submitContact(request);
        assertEquals("Message sent successfully! I'll get back to you soon.", result);
        verify(emailService).sendContactEmail(any(ContactRequest.class), anyString(), anyString(), anyString(), anyString());
    }

}
