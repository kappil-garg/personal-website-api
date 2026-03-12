package com.kapil.personalwebsite.ai.blog;

import com.kapil.personalwebsite.ai.dto.BlogAskRequest;
import com.kapil.personalwebsite.ai.dto.BlogAskResponse;
import com.kapil.personalwebsite.entity.Blog;
import com.kapil.personalwebsite.service.blog.BlogPublicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Implementation of BlogAskService using Spring AI ChatClient with blog content as context (simple RAG).
 *
 * @author Kapil Garg
 */
@Service
public class BlogAskServiceImpl implements BlogAskService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlogAskServiceImpl.class);

    private static final int MAX_CONTEXT_LENGTH = 12_000;
    private static final Pattern HTML_TAG = Pattern.compile("<[^>]+>");

    private static final String SYSTEM_PROMPT = """
            You are a helpful assistant that answers questions using only the provided context from a blog post.
            If the answer cannot be found in the context, say that the information is not available in the blog post.
            Provide a concise answer.
            """;

    private final BlogPublicService blogPublicService;
    private final ChatClient chatClient;

    public BlogAskServiceImpl(BlogPublicService blogPublicService, ChatClient.Builder chatClientBuilder) {
        this.blogPublicService = blogPublicService;
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

    @Override
    public Optional<BlogAskResponse> ask(String slug, BlogAskRequest request) {
        Blog blog = getPublishedBlogOrNull(slug);
        String context = buildContext(blog);
        String question = request.question() != null ? request.question() : "";
        LOGGER.debug("Blog Q&A: slug={}, question length={}", slug, question.length());
        String userMessage = buildUserMessage(blog, context, question);
        String answer = chatClient.prompt()
                .user(userMessage)
                .call()
                .content();
        if (answer == null || answer.isBlank()) {
            return Optional.of(new BlogAskResponse("I couldn't generate an answer. Please try rephrasing your question."));
        }
        return Optional.of(new BlogAskResponse(answer.trim()));
    }

    @Override
    public Flux<String> askStream(String slug, BlogAskRequest request) {
        Blog blog = getPublishedBlogOrNull(slug);
        String context = buildContext(blog);
        String question = request.question() != null ? request.question() : "";
        LOGGER.debug("Blog Q&A (stream): slug={}, question length={}", slug, question.length());
        String userMessage = buildUserMessage(blog, context, question);
        return chatClient.prompt()
                .user(userMessage)
                .stream()
                .content();
    }

    /**
     * Retrieves the published blog by slug, or returns null if not found.
     *
     * @param slug the slug of the blog to retrieve
     * @return the published Blog if found, or null if not found
     */
    private Blog getPublishedBlogOrNull(String slug) {
        Optional<Blog> blogOpt = blogPublicService.getPublishedBlogBySlug(slug);
        return blogOpt.orElse(null);
    }

    /**
     * Builds a plain text context from the blog content by stripping HTML tags and truncating to a max length.
     *
     * @param blog the blog post to extract context from
     * @return a cleaned and truncated text context for AI input
     */
    private String buildContext(Blog blog) {
        String raw = blog.getContent() != null ? blog.getContent() : "";
        String text = HTML_TAG.matcher(raw).replaceAll(" ");
        text = text.replaceAll("\\s+", " ").trim();
        if (text.length() > MAX_CONTEXT_LENGTH) {
            text = text.substring(0, MAX_CONTEXT_LENGTH) + "...";
        }
        return text;
    }

    /**
     * Builds the user message for the AI prompt by combining the blog title, context, and user's question.
     *
     * @param blog     the blog post being asked about
     * @param context  the cleaned and truncated blog content to use as context
     * @param question the user's question about the blog post
     * @return a formatted string containing the blog title, context, and question for the AI prompt
     */
    private String buildUserMessage(Blog blog, String context, String question) {
        return "Blog post:\n\nTitle: " + blog.getTitle() + "\n\nContent:\n" + context
                + "\n\nQuestion: " + question;
    }

}
