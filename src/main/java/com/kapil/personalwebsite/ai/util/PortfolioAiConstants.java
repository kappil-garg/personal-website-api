package com.kapil.personalwebsite.ai.util;

import com.kapil.personalwebsite.util.AppConstants;

/**
 * Shared string constants for portfolio AI (chat prompts and RAG context building).
 * Keeps prompts and section labels in one place for easier tuning and consistency.
 *
 * @author Kapil Garg
 */
public final class PortfolioAiConstants {

    /**
     * System instruction for the portfolio assistant.
     */
    public static final String CHAT_SYSTEM_PROMPT = """
            You are a helpful assistant that answers questions about Kapil Garg's professional
            experience, skills, projects, certifications, education, and published blog posts.
            Use ONLY the provided portfolio context and the "Most relevant portfolio items" section.
            If the information is not present there, say that you are not sure or that it is not available.
            For blog-related questions: you may list topics and post titles from the summary when given.
            When the visitor wants more detail about a specific post, suggest they go to the Blogs
            page, open that post (using its title or slug), and optionally use the chat on that
            post for questions about its content.
            Provide concise, clear answers appropriate for a personal portfolio website visitor.
            """;

    /**
     * User message template for String.formatted: %s = portfolio context, %s = visitor question.
     */
    public static final String CHAT_USER_MESSAGE_TEMPLATE = """
            Use the following portfolio information about Kapil Garg to answer the visitor's question.
            
            Portfolio context:
            %s
            
            Visitor question: %s
            """;

    /**
     * Shown when the model returns null or blank.
     */
    public static final String CHAT_FALLBACK_REPLY =
            "I couldn't generate an answer right now. Please try again or rephrase your question.";

    /**
     * Header before the list of relevant documents when embedding retrieval is used.
     */
    public static final String CHAT_RELEVANT_ITEMS_HEADER = "\n\nMost relevant portfolio items:\n";

    private PortfolioAiConstants() {
        throw new UnsupportedOperationException(AppConstants.UTILITY_CLASS_INSTANTIATION_MSG);
    }

}
