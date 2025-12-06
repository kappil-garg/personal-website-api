package com.kapil.personalwebsite.controller;

import com.kapil.personalwebsite.dto.ApiResponse;
import com.kapil.personalwebsite.dto.ContactRequest;
import com.kapil.personalwebsite.service.ContactService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContactControllerTest {

    @Mock
    private ContactService contactService;

    @InjectMocks
    private ContactController contactController;

    private ContactRequest createContactRequest() {
        ContactRequest request = new ContactRequest();
        request.setName("Ted Mosby");
        request.setEmail("ted.mosby@example.com");
        request.setSubject("Test Subject");
        request.setMessage("Test message content");
        return request;
    }

    @Test
    void submitContact_WhenValidRequest_ShouldReturnSuccessResponse() {
        ContactRequest request = createContactRequest();
        String successMessage = "Message sent successfully! I'll get back to you soon.";
        when(contactService.submitContact(request)).thenReturn(successMessage);
        ResponseEntity<ApiResponse<Map<String, String>>> response = contactController.submitContact(request);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(successMessage, response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertEquals(successMessage, response.getBody().getData().get("message"));
        verify(contactService).submitContact(request);
    }

    @Test
    void submitContact_WhenEmailNotConfigured_ShouldReturnFallbackMessage() {
        ContactRequest request = createContactRequest();
        String fallbackMessage = "Thank you for your message. I'll get back to you soon.";
        when(contactService.submitContact(request)).thenReturn(fallbackMessage);
        ResponseEntity<ApiResponse<Map<String, String>>> response = contactController.submitContact(request);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(fallbackMessage, response.getBody().getMessage());
        verify(contactService).submitContact(request);
    }

    @Test
    void submitContact_ShouldReturnApiResponseWrapper() {
        ContactRequest request = createContactRequest();
        String message = "Message sent successfully! I'll get back to you soon.";
        when(contactService.submitContact(request)).thenReturn(message);
        ResponseEntity<ApiResponse<Map<String, String>>> response = contactController.submitContact(request);
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTimestamp());
        assertNull(response.getBody().getStatus());
    }

    @Test
    void submitContact_WithNullSubject_ShouldHandleGracefully() {
        ContactRequest request = createContactRequest();
        request.setSubject(null);
        String message = "Message sent successfully! I'll get back to you soon.";
        when(contactService.submitContact(request)).thenReturn(message);
        ResponseEntity<ApiResponse<Map<String, String>>> response = contactController.submitContact(request);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(contactService).submitContact(request);
    }

}
