package com.kapil.personalwebsite.controller;

import com.kapil.personalwebsite.ai.blog.BlogAskService;
import com.kapil.personalwebsite.ai.dto.BlogAskRequest;
import com.kapil.personalwebsite.ai.dto.BlogAskResponse;
import com.kapil.personalwebsite.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

/**
 * REST controller for AI-powered blog interactions (Q&A).
 * Exposes endpoints for asking questions about published blogs using AI.
 *
 * @author Kapil Garg
 */
@RestController
@RequestMapping("/blogs")
@RequiredArgsConstructor
public class BlogAiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlogAiController.class);

    private final BlogAskService blogAskService;

    /**
     * Answers a question about a published blog post using AI (public access, rate limited).
     *
     * @param slug    the slug of the published blog
     * @param request the request containing the question
     * @return a ResponseEntity containing the answer, or 404 if blog not found
     */
    @PostMapping("/published/{slug}/ask")
    public ResponseEntity<ApiResponse<BlogAskResponse>> askAboutBlog(
            @PathVariable String slug,
            @Valid @RequestBody BlogAskRequest request) {
        LOGGER.info("POST /blogs/published/{}/ask - Blog Q&A requested (public)", slug);
        return blogAskService.ask(slug, request)
                .map(responseData -> {
                    ApiResponse<BlogAskResponse> response = ApiResponse.success(
                            responseData, "Answer generated successfully");
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Blog not found", HttpStatus.NOT_FOUND.value())));
    }

    /**
     * Streams an answer about a published blog post using AI (public access, rate limited).
     * Uses Server-Sent Events (SSE) to send the answer incrementally as it is generated.
     *
     * @param slug     the slug of the published blog
     * @param question the user's question
     * @return an SSE stream of answer chunks
     */
    @GetMapping(value = "/published/{slug}/ask/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter askAboutBlogStream(@PathVariable String slug,
                                         @RequestParam("question") String question) {
        LOGGER.info("GET /blogs/published/{}/ask/stream - Blog Q&A streaming requested (public)", slug);
        SseEmitter emitter = new SseEmitter(60_000L);
        if (question == null || question.isBlank()) {
            try {
                emitter.send(SseEmitter.event().name("error")
                        .data("Question is required and cannot be blank."));
            } catch (IOException ignored) {
                // If we can't send the error event, just complete the emitter
                LOGGER.debug("Failed to send SSE error event for empty question");
            }
            emitter.complete();
            return emitter;
        }
        BlogAskRequest request = new BlogAskRequest(question);
        blogAskService.askStream(slug, request)
                .doOnError(ex -> {
                    try {
                        emitter.send(SseEmitter.event().name("error")
                                .data("Error generating answer."));
                    } catch (IOException ignored) {
                        // Ignore send failures on error path
                        LOGGER.debug("Failed to send SSE error event for streaming failure");
                    }
                    emitter.completeWithError(ex);
                })
                .doOnComplete(emitter::complete)
                .subscribe(chunk -> {
                    try {
                        emitter.send(SseEmitter.event().data(chunk));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                });
        return emitter;
    }

}
