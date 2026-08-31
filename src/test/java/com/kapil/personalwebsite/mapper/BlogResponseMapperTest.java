package com.kapil.personalwebsite.mapper;

import com.kapil.personalwebsite.dto.ApiResponse;
import com.kapil.personalwebsite.entity.Blog;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class BlogResponseMapperTest {

    @Test
    void buildBlogResponse_WhenBlogExists_ShouldReturnSuccessResponse() {
        Blog blog = new Blog("Title", "Content", "test-slug");
        ResponseEntity<ApiResponse<Blog>> response = BlogResponseMapper.buildBlogResponse("test-slug", blog);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Blog with slug 'test-slug' retrieved successfully", response.getBody().getMessage());
        assertEquals(blog, response.getBody().getData());
        assertNotNull(response.getBody().getTimestamp());
        assertNull(response.getBody().getStatus());
    }

    @Test
    void buildBlogResponse_WhenBlogIsMissing_ShouldReturnNotFoundResponse() {
        ResponseEntity<ApiResponse<Blog>> response = BlogResponseMapper.buildBlogResponse("missing-slug", null);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Blog with slug 'missing-slug' not found", response.getBody().getMessage());
        assertNull(response.getBody().getData());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatus());
    }

    @Test
    void buildBlogByIdResponse_WhenBlogExists_ShouldReturnSuccessResponse() {
        Blog blog = new Blog("Title", "Content", "test-slug");
        ResponseEntity<ApiResponse<Blog>> response = BlogResponseMapper.buildBlogByIdResponse("123", blog);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Blog with ID '123' retrieved successfully", response.getBody().getMessage());
        assertEquals(blog, response.getBody().getData());
    }

    @Test
    void buildBlogByIdResponse_WhenBlogIsMissing_ShouldReturnNotFoundResponse() {
        ResponseEntity<ApiResponse<Blog>> response = BlogResponseMapper.buildBlogByIdResponse("123", null);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Blog with ID '123' not found", response.getBody().getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatus());
    }

}
