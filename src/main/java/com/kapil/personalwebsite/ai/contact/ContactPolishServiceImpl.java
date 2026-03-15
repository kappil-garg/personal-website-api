package com.kapil.personalwebsite.ai.contact;

import com.kapil.personalwebsite.ai.dto.ContactPolishRequest;
import com.kapil.personalwebsite.ai.dto.ContactPolishResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Implementation of ContactPolishService using Spring AI ChatClient to suggest improved contact form message.
 *
 * @author Kapil Garg
 */
@Service
public class ContactPolishServiceImpl implements ContactPolishService {

    private static final String SYSTEM_PROMPT = """
            You are a helpful writing assistant.
            Improve the user's message for a professional contact form.
            Fix grammar and spelling, and improve clarity and conciseness while maintaining a polite professional tone.
            Preserve the user's original intent and key points and do not add new information.
            Return only the improved message with no explanation or preamble.
            Keep the final message under 2000 characters.
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
