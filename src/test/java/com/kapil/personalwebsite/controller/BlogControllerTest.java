package com.kapil.personalwebsite.controller;

import com.kapil.personalwebsite.entity.Blog;
import com.kapil.personalwebsite.service.BlogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlogControllerTest {

    @Mock
    private BlogService blogService;

    @InjectMocks
    private BlogController blogController;

    @Test
    void getAllBlogs_ShouldReturnBlogsList() {
        Blog blog1 = new Blog("Test Blog 1", "Content 1", "test-blog-1");
        Blog blog2 = new Blog("Test Blog 2", "Content 2", "test-blog-2");
        List<Blog> blogs = Arrays.asList(blog1, blog2);
        when(blogService.getAllBlogs()).thenReturn(blogs);
        var response = blogController.getAllBlogs();
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Test Blog 1", response.getBody().getFirst().getTitle());
    }

    @Test
    void getBlogBySlug_WhenBlogExists_ShouldReturnBlog() {
        Blog blog = new Blog("Test Blog", "Test Content", "test-blog");
        when(blogService.getBlogBySlug("test-blog")).thenReturn(Optional.of(blog));
        var response = blogController.getBlogBySlug("test-blog");
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals("Test Blog", response.getBody().getTitle());
    }

    @Test
    void getBlogBySlug_WhenBlogNotFound_ShouldReturn404() {
        when(blogService.getBlogBySlug("non-existent")).thenReturn(Optional.empty());
        var response = blogController.getBlogBySlug("non-existent");
        assertNotNull(response);
        assertNull(response.getBody());
    }

}
