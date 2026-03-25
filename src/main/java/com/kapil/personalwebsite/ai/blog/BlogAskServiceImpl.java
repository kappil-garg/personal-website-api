package com.kapil.personalwebsite.ai.blog;

import com.kapil.personalwebsite.ai.dto.BlogAskRequest;
import com.kapil.personalwebsite.ai.dto.BlogAskResponse;
import com.kapil.personalwebsite.ai.util.AiTextUtils;
import com.kapil.personalwebsite.ai.util.BlogAiConstants;
import com.kapil.personalwebsite.entity.Blog;
import com.kapil.personalwebsite.service.blog.BlogPublicService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Optional;

/**
 * Implementation of BlogAskService using Spring AI ChatClient with blog content as context (simple RAG).
 *
 * @author Kapil Garg
 */
@Service
public class BlogAskServiceImpl implements BlogAskService {

    private static final int MAX_CONTEXT_LENGTH = 12_000;

    private final BlogPublicService blogPublicService;
    private final ChatClient chatClient;

    public BlogAskServiceImpl(BlogPublicService blogPublicService, ChatClient.Builder chatClientBuilder) {
        this.blogPublicService = blogPublicService;
        this.chatClient = chatClientBuilder
                .defaultSystem(BlogAiConstants.SYSTEM_PROMPT)
                .build();
    }

    @Override
    public Optional<BlogAskResponse> ask(String slug, BlogAskRequest request) {
        Optional<Blog> blogOpt = blogPublicService.getPublishedBlogBySlug(slug);
        if (blogOpt.isEmpty()) {
            return Optional.empty();
        }
        Blog blog = blogOpt.get();
        String context = buildContext(blog);
        String question = request.question() != null ? request.question() : "";
        String userMessage = buildUserMessage(blog, context, question);
        String answer = chatClient.prompt()
                .user(userMessage)
                .call()
                .content();
        if (answer == null || answer.isBlank()) {
            return Optional.of(new BlogAskResponse(BlogAiConstants.FALLBACK_REPLY));
        }
        return Optional.of(new BlogAskResponse(answer.trim()));
    }

    @Override
    public Flux<String> askStream(String slug, BlogAskRequest request) {
        Optional<Blog> blogOpt = blogPublicService.getPublishedBlogBySlug(slug);
        if (blogOpt.isEmpty()) {
            return Flux.error(new IllegalArgumentException(
                    String.format("Published blog with slug '%s' not found", slug)
            ));
        }
        Blog blog = blogOpt.get();
        String context = buildContext(blog);
        String question = request.question() != null ? request.question() : "";
        String userMessage = buildUserMessage(blog, context, question);
        return chatClient.prompt()
                .user(userMessage)
                .stream()
                .content();
    }

    /**
     * Builds a plain text context from the blog content by stripping HTML tags and truncating to a max length.
     *
     * @param blog the blog post to extract context from
     * @return a cleaned and truncated text context for AI input
     */
    private String buildContext(Blog blog) {
        String text = AiTextUtils.stripHtmlTags(blog.getContent() != null ? blog.getContent() : "");
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
        String title = blog.getTitle() != null ? blog.getTitle() : "";
        return BlogAiConstants.USER_MESSAGE_TEMPLATE.formatted(title, context, question);
    }

}
