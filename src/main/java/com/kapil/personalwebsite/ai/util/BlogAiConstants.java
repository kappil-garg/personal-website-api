package com.kapil.personalwebsite.ai.util;

import com.kapil.personalwebsite.util.AppConstants;

/**
 * Shared string constants for blog AI (Q&A over a single blog post).
 * Keeps prompts and message templates in one place for tuning and consistency.
 *
 * @author Kapil Garg
 */
public final class BlogAiConstants {

    /**
     * System instruction for the blog Q&A assistant.
     */
    public static final String SYSTEM_PROMPT = """
            You are a helpful assistant that answers questions using only the provided context from a blog post.
            If the answer cannot be found in the context, say that the information is not available in the blog post.
            Provide a concise answer.
            """;

    /**
     * User message template for String.formatted: %s = title, %s = context, %s = question.
     */
    public static final String USER_MESSAGE_TEMPLATE = """
            Blog post:
            
            Title: %s
            
            Content:
            %s
            
            Question: %s
            """;

    /**
     * Shown when the model returns null or blank.
     */
    public static final String FALLBACK_REPLY =
            "I couldn't generate an answer. Please try rephrasing your question.";

    private BlogAiConstants() {
        throw new UnsupportedOperationException(AppConstants.UTILITY_CLASS_INSTANTIATION_MSG);
    }

}
