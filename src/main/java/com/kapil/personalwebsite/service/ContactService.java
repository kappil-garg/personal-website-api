package com.kapil.personalwebsite.service;

import com.kapil.personalwebsite.dto.ContactRequest;

/**
 * Service interface for contact form operations.
 * Handles contact form submissions and email notifications.
 *
 * @author Kapil Garg
 */
public interface ContactService {

    /**
     * Processes a contact form submission and sends email notification.
     *
     * @param contactRequest the contact form data
     * @return success message
     */
    String submitContact(ContactRequest contactRequest);

}
