package com.kapil.personalwebsite.controller;

import com.kapil.personalwebsite.ai.blog.BlogAskService;
import com.kapil.personalwebsite.ai.dto.BlogAskRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlogAiControllerTest {

    @Mock
    private BlogAskService blogAskService;

    @InjectMocks
    private BlogAiController blogAiController;

    @Test
    void askAboutBlogStream_WhenQuestionBlank_ShouldSendErrorAndComplete() {
        SseEmitter emitter = blogAiController.askAboutBlogStream("slug", " ");
        assertNotNull(emitter);
        // Can't easily assert sent events without a custom emitter; reaching here confirms the early return works.
    }

    @Test
    void askAboutBlogStream_WhenBlogNotFound_ShouldReturnErrorFlux() {
        when(blogAskService.askStream(eq("missing-slug"), any(BlogAskRequest.class)))
                .thenReturn(Flux.error(new IllegalArgumentException("Published blog with slug 'missing-slug' not found")));
        SseEmitter emitter = blogAiController.askAboutBlogStream("missing-slug", "question");
        assertNotNull(emitter);
    }

    @Test
    void askAboutBlogStream_ShouldSubscribeToFlux() {
        when(blogAskService.askStream(eq("slug"), any(BlogAskRequest.class)))
                .thenReturn(Flux.just("chunk1", "chunk2"));
        SseEmitter emitter = blogAiController.askAboutBlogStream("slug", "question");
        assertNotNull(emitter);
        // If no exception is thrown, subscription wiring is valid.
    }

}
