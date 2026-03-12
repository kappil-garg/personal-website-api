package com.kapil.personalwebsite.ai.contact;

import com.kapil.personalwebsite.ai.dto.ContactPolishRequest;
import com.kapil.personalwebsite.ai.dto.ContactPolishResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Implementation of ContactPolishService using Spring AI ChatClient to suggest improved contact form message.
 *
 * @author Kapil Garg
 */
@Service
public class ContactPolishServiceImpl implements ContactPolishService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContactPolishServiceImpl.class);

    private static final String SYSTEM_PROMPT = """
            You are a helpful writing assistant. Improve the user's message for a professional contact form.
            Fix grammar and spelling, improve clarity and conciseness, and use a polite professional tone.
            Preserve the user's intent and key points. Reply with only the improved message, no preamble or explanation.
            Keep the message under 2000 characters.
            """;

    private final ChatClient chatClient;

    public ContactPolishServiceImpl(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

    @Override
    public ContactPolishResponse polishMessage(ContactPolishRequest request) {
        String message = request.message() != null ? request.message() : "";
        LOGGER.debug("Polishing contact message (length={})", message.length());
        String suggested = chatClient.prompt()
                .user(message)
                .call()
                .content();
        if (suggested != null) {
            suggested = suggested.trim();
            if (suggested.length() > 2000) {
                suggested = suggested.substring(0, 2000);
            }
        } else {
            suggested = message;
        }
        return new ContactPolishResponse(suggested);
    }

}
