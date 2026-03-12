package com.kapil.personalwebsite.ai.contact;

import com.kapil.personalwebsite.ai.dto.ContactPolishRequest;
import com.kapil.personalwebsite.ai.dto.ContactPolishResponse;

/**
 * Service for AI-assisted contact message polish (grammar, clarity, professional tone).
 *
 * @author Kapil Garg
 */
public interface ContactPolishService {

    /**
     * Returns an improved version of the given message suitable for a professional contact form.
     *
     * @param request the polish request containing the original message
     * @return response with the suggested message
     */
    ContactPolishResponse polishMessage(ContactPolishRequest request);

}
