package com.kapil.personalwebsite.controller;

import com.kapil.personalwebsite.dto.ApiResponse;
import com.kapil.personalwebsite.dto.ContactRequest;
import com.kapil.personalwebsite.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller for handling contact form submissions.
 * Provides an endpoint for public contact form submission functionality.
 *
 * @author Kapil Garg
 */
@RestController
@RequestMapping("/contact")
@RequiredArgsConstructor
public class ContactController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContactController.class);

    private final ContactService contactService;

    /**
     * Submits a contact form and sends email notification (public access).
     *
     * @param contactRequest the contact form data
     * @return a ResponseEntity containing the success message
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> submitContact(@Valid @RequestBody ContactRequest contactRequest) {
        LOGGER.info("POST /contact - Contact form submission received");
        String message = contactService.submitContact(contactRequest);
        Map<String, String> responseData = Map.of("message", message);
        ApiResponse<Map<String, String>> response = ApiResponse.success(responseData, message);
        return ResponseEntity.ok(response);
    }

}
