package com.kapil.personalwebsite.service;

import com.kapil.personalwebsite.entity.Blog;
import com.kapil.personalwebsite.repository.BlogRepository;
import com.kapil.personalwebsite.service.impl.BlogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlogServiceTest {

    @Mock
    private BlogRepository blogRepository;

    @InjectMocks
    private BlogServiceImpl blogService;

    private Blog testBlog;

    @BeforeEach
    void setUp() {
        testBlog = new Blog("Test Blog", "Test Content", "test-blog");
    }

    @Test
    void getAllBlogs_ShouldReturnAllActiveBlogs() {
        List<Blog> blogs = Collections.singletonList(testBlog);
        when(blogRepository.findByIsActiveTrueOrderByCreatedAtDesc()).thenReturn(blogs);
        List<Blog> result = blogService.getAllBlogs();
        assertEquals(1, result.size());
        assertEquals("Test Blog", result.getFirst().getTitle());
        verify(blogRepository).findByIsActiveTrueOrderByCreatedAtDesc();
    }

    @Test
    void getBlogBySlug_WhenBlogExists_ShouldReturnBlog() {
        when(blogRepository.findBySlugAndIsActiveTrue("test-blog")).thenReturn(Optional.of(testBlog));
        Optional<Blog> result = blogService.getBlogBySlug("test-blog");
        assertTrue(result.isPresent());
        assertEquals("Test Blog", result.get().getTitle());
        verify(blogRepository).findBySlugAndIsActiveTrue("test-blog");
    }

    @Test
    void getBlogBySlug_WhenBlogNotFound_ShouldReturnEmpty() {
        when(blogRepository.findBySlugAndIsActiveTrue("non-existent")).thenReturn(Optional.empty());
        Optional<Blog> result = blogService.getBlogBySlug("non-existent");
        assertTrue(result.isEmpty());
        verify(blogRepository).findBySlugAndIsActiveTrue("non-existent");
    }

    @Test
    void createBlog_WhenSlugDoesNotExist_ShouldCreateBlog() {
        when(blogRepository.existsBySlug("test-blog")).thenReturn(false);
        when(blogRepository.save(any(Blog.class))).thenReturn(testBlog);
        Blog result = blogService.createBlog(testBlog);
        assertEquals("Test Blog", result.getTitle());
        verify(blogRepository).existsBySlug("test-blog");
        verify(blogRepository).save(testBlog);
    }

    @Test
    void createBlog_WhenSlugExists_ShouldThrowException() {
        when(blogRepository.existsBySlug("test-blog")).thenReturn(true);
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> blogService.createBlog(testBlog)
        );
        assertEquals("Blog with slug 'test-blog' already exists", exception.getMessage());
        verify(blogRepository).existsBySlug("test-blog");
        verify(blogRepository, never()).save(any(Blog.class));
    }

}
