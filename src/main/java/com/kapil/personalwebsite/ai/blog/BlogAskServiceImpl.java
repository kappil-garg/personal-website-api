package com.kapil.personalwebsite.ai.blog;

import com.kapil.personalwebsite.ai.dto.BlogAskRequest;
import com.kapil.personalwebsite.ai.dto.BlogAskResponse;
import com.kapil.personalwebsite.entity.Blog;
import com.kapil.personalwebsite.service.blog.BlogPublicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

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
            You are a helpful assistant that answers questions based only on the provided blog post content.
            Use only information from the blog post. If the answer is not in the post, say so clearly. Be concise.
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
        Optional<Blog> blogOpt = blogPublicService.getPublishedBlogBySlug(slug);
        if (blogOpt.isEmpty()) {
            return Optional.empty();
        }
        Blog blog = blogOpt.get();
        String context = buildContext(blog);
        String question = request.question() != null ? request.question() : "";
        LOGGER.debug("Blog Q&A: slug={}, question length={}", slug, question.length());
        String userMessage = "Blog post:\n\nTitle: " + blog.getTitle() + "\n\nContent:\n" + context
                + "\n\nQuestion: " + question;
        String answer = chatClient.prompt()
                .user(userMessage)
                .call()
                .content();
        if (answer == null || answer.isBlank()) {
            return Optional.of(new BlogAskResponse("I couldn't generate an answer. Please try rephrasing your question."));
        }
        return Optional.of(new BlogAskResponse(answer.trim()));
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

}
