package com.kapil.personalwebsite.controller;

import com.kapil.personalwebsite.entity.Blog;
import com.kapil.personalwebsite.service.blog.BlogAdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlogControllerTest {

    @Mock
    private BlogAdminService blogAdminService;

    @InjectMocks
    private BlogController blogController;

    @Test
    void getAllBlogs_ShouldReturnBlogsList() {
        Blog blog1 = new Blog("Test Blog 1", "Content 1", "test-blog-1");
        Blog blog2 = new Blog("Test Blog 2", "Content 2", "test-blog-2");
        List<Blog> blogs = Arrays.asList(blog1, blog2);
        when(blogAdminService.getAllBlogs()).thenReturn(blogs);
        var response = blogController.getAllBlogs();
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
        assertEquals(2, response.getBody().getData().size());
        assertEquals("Test Blog 1", response.getBody().getData().getFirst().getTitle());
    }

    @Test
    void getBlogBySlug_WhenBlogExists_ShouldReturnBlog() {
        Blog blog = new Blog("Test Blog", "Test Content", "test-blog");
        when(blogAdminService.getBlogBySlug("test-blog")).thenReturn(Optional.of(blog));
        var response = blogController.getBlogBySlug("test-blog");
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
        assertEquals("Test Blog", response.getBody().getData().getTitle());
    }

    @Test
    void getBlogBySlug_WhenBlogNotFound_ShouldReturn404() {
        when(blogAdminService.getBlogBySlug("non-existent")).thenReturn(Optional.empty());
        var response = blogController.getBlogBySlug("non-existent");
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode().value());
    }

}
